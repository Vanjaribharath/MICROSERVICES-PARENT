/**
 * Day 28 — k6 Performance Test: Kafka + Saga flow
 * Tests the complete product creation flow including Saga + Kafka event
 * Run: k6 run kafka-saga-test.js
 */
import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const sagaErrors     = new Counter('saga_errors');
const eventWriteRate = new Rate('event_write_success');
const sagaDuration   = new Trend('saga_create_duration');

export const options = {
    scenarios: {
        // Scenario 1: Normal load
        normal_load: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 10 },
                { duration: '1m',  target: 20 },
                { duration: '30s', target: 0  },
            ],
            gracefulRampDown: '10s',
        },
        // Scenario 2: Saga stress test
        saga_stress: {
            executor: 'constant-arrival-rate',
            rate: 5,
            timeUnit: '1s',
            duration: '1m',
            preAllocatedVUs: 10,
            startTime: '30s',
        },
    },
    thresholds: {
        http_req_duration:   ['p(95)<800'],
        http_req_failed:     ['rate<0.02'],
        saga_create_duration: ['p(95)<1000'],
    },
};

const BASE  = __ENV.BASE_URL  || 'http://localhost:8081';
const ADMIN_TOKEN = __ENV.ADMIN_TOKEN || '';

export function setup() {
    // Get JWT token
    const resp = http.post('http://localhost:8080/auth/token',
        JSON.stringify({ username: 'admin', role: 'ADMIN' }),
        { headers: { 'Content-Type': 'application/json' } });
    if (resp.status === 200) return { token: JSON.parse(resp.body).token };
    return { token: ADMIN_TOKEN };
}

export default function (data) {
    const headers = {
        'Content-Type':    'application/json',
        'Authorization':   `Bearer ${data.token}`,
        'X-Correlation-ID': `k6-${__VU}-${__ITER}-${Date.now()}`,
    };

    group('Saga: Create Product', () => {
        const start = Date.now();
        const resp = http.post(`${BASE}/api/v1/products`,
            JSON.stringify({
                name:       `k6-product-${__VU}-${__ITER}`,
                price:      (Math.random() * 1000 + 10).toFixed(2),
                categoryId: 1,
            }),
            { headers });

        sagaDuration.add(Date.now() - start);

        const ok = check(resp, {
            'saga: status 201':      (r) => r.status === 201,
            'saga: has X-Saga-Id':   (r) => r.headers['X-Saga-Id'] !== undefined,
            'saga: correlationId':   (r) => r.headers['X-Correlation-ID'] !== undefined,
        });
        if (!ok) sagaErrors.add(1);
    });

    group('Verify: Event in DB', () => {
        // Check event was written to domain_events
        const resp = http.get(`${BASE}/api/v1/events/type/PRODUCT_CREATED`,
            { headers });
        const ok = check(resp, {
            'events: status 200':    (r) => r.status === 200,
            'events: has records':   (r) => {
                try { return JSON.parse(r.body).length > 0; } catch { return false; }
            },
        });
        eventWriteRate.add(ok ? 1 : 0);
    });

    group('Verify: Saga in DB', () => {
        const resp = http.get(`${BASE}/api/v1/sagas/status/COMPLETED`,
            { headers });
        check(resp, {
            'sagas completed: 200': (r) => r.status === 200,
        });
    });

    sleep(1);
}
