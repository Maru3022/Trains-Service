import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 100 },
        { duration: '1m', target: 500 },
        { duration: '2m', target: 1500 },
        { duration: '1m', target: 0 },
    ],
};

export default function () {
    const url = 'http://127.0.0.1:8035/api/trains';
    const payload = JSON.stringify({
        name: `Train_${__VU}_${Math.floor(Math.random() * 100000)}`,
        category: 'Test',
        durationMinutes: 60
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
        timeout: '10s',
    };

    const res = http.post(url, payload, params);

    check(res, {
        'status is 201 or 200': (r) => r.status === 200 || r.status === 201,
    });

    sleep(Math.random() * 1 + 0.5);
}