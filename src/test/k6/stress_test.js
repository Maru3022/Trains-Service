import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '1m', target: 500 },   // Плавный прогрев
        { duration: '2m', target: 2000 },  // Разгон до 2к
        { duration: '3m', target: 5000 },  // Разгон до 5к
        { duration: '1m', target: 0 },
    ],
};

export default function () {
    const url = 'http://localhost:8009/api/trains';
    const payload = JSON.stringify({
        name: `Train_${__VU}`,
        category: 'Test',
        durationMinutes: 60
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
        timeout: '10s',
    };

    const res = http.post(url, payload, params);
    check(res, { 'status is 200': (r) => r.status === 200 });

    sleep(1); // ПАУЗА 1 секунда (обязательно!)
}
//k6 run src/test/k6/stress_test.js