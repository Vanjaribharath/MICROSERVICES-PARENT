// Day 28 — k6 Smoke Test (quick sanity check)
// Run: k6 run smoke-test.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 1,
    duration: '30s',
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(99)<1000'],
    },
};

export default function () {
    // System API health
    check(http.get('http://localhost:8081/actuator/health/liveness'), {
        'system-api liveness UP': (r) => r.status === 200,
    });
    // Middleware health
    check(http.get('http://localhost:8082/actuator/health/liveness'), {
        'middleware liveness UP': (r) => r.status === 200,
    });
    // BFF health
    check(http.get('http://localhost:8080/actuator/health/liveness'), {
        'bff-gateway liveness UP': (r) => r.status === 200,
    });
}
