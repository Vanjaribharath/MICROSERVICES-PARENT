/**
 * Day 28 — Full load test: all 3 layers
 * Tests BFF → Middleware → System API flow under load
 */
import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const errorCount  = new Counter('errors');
const errorRate   = new Rate('error_rate');
const createTime  = new Trend('product_create_duration');
const enrichTime  = new Trend('enrichment_duration');

export const options = {
    stages: [
        { duration: '30s', target: 10  },
        { duration: '1m',  target: 50  },
        { duration: '30s', target: 100 },
        { duration: '30s', target: 0   },
    ],
    thresholds: {
        http_req_duration:       ['p(95)<500'],
        http_req_failed:         ['rate<0.01'],
        product_create_duration: ['p(95)<800'],
        enrichment_duration:     ['p(95)<600'],
    },
};

const SYSTEM  = __ENV.SYSTEM_URL  || 'http://localhost:8081';
const MIDDLE  = __ENV.MIDDLE_URL  || 'http://localhost:8082';
const BFF     = __ENV.BASE_URL    || 'http://localhost:8080';

export function setup() {
    const resp = http.post(`${BFF}/auth/token`,
        JSON.stringify({ username: 'user', role: 'USER' }),
        { headers: { 'Content-Type': 'application/json' } });
    if (resp.status === 200) return { token: JSON.parse(resp.body).token };
    return { token: '' };
}

export default function (data) {
    const corrId = `k6-${__VU}-${__ITER}`;
    const headers = {
        'Content-Type':    'application/json',
        'Authorization':   `Bearer ${data.token}`,
        'X-Correlation-ID': corrId,
    };

    group('system-api: list products', () => {
        const r = http.get(`${SYSTEM}/api/v1/products?page=0&size=10`);
        check(r, { '200 OK': (r) => r.status === 200 }) || errorCount.add(1);
        errorRate.add(r.status !== 200);
    });

    group('middleware: enriched product', () => {
        const s = Date.now();
        const r = http.get(`${MIDDLE}/api/v1/catalog/products/1`);
        enrichTime.add(Date.now() - s);
        check(r, {
            '200 or 404': (r) => [200, 404].includes(r.status),
            'has taxAmount': (r) => {
                if (r.status !== 200) return true;
                try { return JSON.parse(r.body).data?.taxAmount !== undefined; }
                catch { return false; }
            },
        });
    });

    group('bff-gateway: catalog overview', () => {
        const r = http.get(`${BFF}/api/v1/catalog/overview`, { headers });
        check(r, { 'auth OK': (r) => r.status !== 401 });
    });

    sleep(0.5);
}
