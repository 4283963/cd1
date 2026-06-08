const API_BASE_URL = 'http://localhost:8080/api';

const api = {
    async request(endpoint, options = {}) {
        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                ...options
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error(`API Error [${endpoint}]:`, error);
            throw error;
        }
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
            method: 'PUT'
        });
    },

    async getCatRanking(limit = 10) {
        return this.request(`/statistics/cat-ranking?limit=${limit}`);
    },

    async getNewCatsTodayCount() {
        return this.request('/cats/stats/new-today');
    }
};
