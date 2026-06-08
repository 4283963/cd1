const API_BASE_URL = 'http://localhost:8080/api';
const DEFAULT_TIMEOUT = 10000;
const MAX_RETRY_COUNT = 1;

class RequestController {
    constructor() {
        this.pendingRequests = new Map();
    }

    generateId() {
        return Date.now().toString(36) + Math.random().toString(36).substr(2);
    }

    cancelAll() {
        this.pendingRequests.forEach((controller, id) => {
            controller.abort();
        });
        this.pendingRequests.clear();
    }
}

const requestController = new RequestController();

const api = {
    async request(endpoint, options = {}) {
        const requestId = requestController.generateId();
        const controller = new AbortController();
        requestController.pendingRequests.set(requestId, controller);

        const timeout = options.timeout || DEFAULT_TIMEOUT;
        const timeoutId = setTimeout(() => {
            controller.abort();
        }, timeout);

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                signal: controller.signal,
                ...options
            });

            clearTimeout(timeoutId);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                const error = new Error(errorData.message || `请求失败 (${response.status})`);
                error.status = response.status;
                error.data = errorData;
                throw error;
            }

            return await response.json();

        } catch (error) {
            if (error.name === 'AbortError' || error.message.includes('aborted') || error.message.includes('Aborted') {
                const timeoutError = new Error('请求超时，请检查网络连接后重试');
                timeoutError.isTimeout = true;
                timeoutError.status = 504;
                throw timeoutError;
            }

            if (!error.status) {
                error.status = 0;
                error.message = '网络连接失败，请检查网络';
            }

            throw error;

        } finally {
            requestController.pendingRequests.delete(requestId);
            clearTimeout(timeoutId);
        }
    },

    async requestWithRetry(endpoint, options = {}) {
        const maxRetries = options.retry !== undefined ? options.retry : MAX_RETRY_COUNT;
        let lastError;

        for (let i = 0; i <= maxRetries; i++) {
            try {
                return await this.request(endpoint, { ...options, retry: undefined });
            } catch (error) {
                lastError = error;
                if (error.isTimeout && error.isTimeout) {
                    break;
                }
                if (i < maxRetries) {
                    await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
                }
            }
        }

        throw lastError;
    },

    async getDashboardStats() {
        return this.request('/statistics/dashboard');
    },

    async getFeeders(filter = 'all') {
        let url = '/feeders';
        if (filter === 'alert') {
            url += '?alerts=true';
        } else if (filter === 'online') {
            url += '?status=online';
        }
        return this.request(url);
    },

    async getFeederByCode(code) {
        return this.request(`/feeders/code/${code}`);
    },

    async getFeederSensorData(code, hours = 24) {
        return this.request(`/feeders/${code}/sensor-data?hours=${hours}`);
    },

    async supplyFeeder(feederCode, foodAmount, waterAmount, operator = '志愿者') {
        return this.requestWithRetry('/feeders/supply', {
            method: 'POST',
            body: JSON.stringify({
                feederCode,
                foodAmount,
                waterAmount,
                operator
            }),
            timeout: 15000
        });
    },

    async getCats(filter = 'all', hours = 24) {
        let url = '/cats';
        if (filter === 'new') {
            url += '?type=new';
        } else if (filter === 'recent') {
            url += `?type=recent&hours=${hours}`;
        }
        return this.request(url);
    },

    async getCatById(id) {
        return this.request(`/cats/${id}`);
    },

    async getCatCaptures(id) {
        return this.request(`/cats/${id}/captures`);
    },

    async getAlerts(active = true, type = null, days = 7) {
        let url = `/alerts?active=${active}&days=${days}`;
        if (type) {
            url += `&type=${type}`;
        }
        return this.request(url);
    },

    async getAlertCount() {
        return this.request('/alerts/count');
    },

    async resolveAlert(id) {
        return this.request(`/alerts/${id}/resolve`, {
            method: 'PUT',
            timeout: 8000
        });
    },

    async getCatRanking(limit = 10) {
        return this.request(`/statistics/cat-ranking?limit=${limit}`);
    },

    async getNewCatsTodayCount() {
        return this.request('/cats/stats/new-today');
    },

    cancelAllRequests() {
        requestController.cancelAll();
    }
};

function showToast(message, type = 'info') {
    const existing = document.querySelector('.toast-notification');
    if (existing) {
        existing.remove();
    }

    const toast = document.createElement('div');
    toast.className = `toast-notification toast-${type}`;
    toast.innerHTML = `
        <span class="toast-icon">${type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ'}</span>
        <span class="toast-message">${message}</span>
    `;
    document.body.appendChild(toast);

    requestAnimationFrame(() => {
        toast.classList.add('toast-show');
    });

    setTimeout(() => {
        toast.classList.remove('toast-show');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}
