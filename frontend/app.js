let currentPage = 'home';
let catFilter = 'all';
let feederFilter = 'all';
let alertFilter = 'active';

function showPage(pageName) {
    document.querySelectorAll('.page').forEach(page => {
        page.classList.add('hidden');
    });

    const targetPage = document.getElementById(`page-${pageName}`);
    if (targetPage) {
        targetPage.classList.remove('hidden');
    }

    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
        if (btn.dataset.page === pageName) {
            btn.classList.add('active');
        }
    });

    currentPage = pageName;

    loadPageData(pageName);

    window.scrollTo(0, 0);
}

function loadPageData(pageName) {
    switch (pageName) {
        case 'home':
            loadHomePage();
            break;
        case 'feeders':
            loadFeedersPage();
            break;
        case 'cats':
            loadCatsPage();
            break;
        case 'alerts':
            loadAlertsPage();
            break;
    }
}

async function loadHomePage() {
    try {
        const [stats, newCats, feeders, ranking, alertCount] = await Promise.all([
            api.getDashboardStats().catch(() => getMockDashboardStats()),
            api.getCats('new').catch(() => getMockNewCats()),
            api.getFeeders().catch(() => getMockFeeders()),
            api.getCatRanking(5).catch(() => getMockRanking()),
            api.getAlertCount().catch(() => getMockAlertCount())
        ]);

        renderDashboardStats(stats);
        renderAlertBanner(alertCount);
        renderNewCats(newCats.cats || []);
        renderFeederListMini(feeders.feeders || []);
        renderRanking(ranking);
    } catch (error) {
        console.error('加载首页数据失败:', error);
        loadMockHomeData();
    }
}

function renderDashboardStats(stats) {
    document.getElementById('stat-total-cats').textContent = stats.totalCats || '--';
    document.getElementById('stat-captures-today').textContent = stats.capturesToday || '--';
    document.getElementById('stat-new-cats').textContent = stats.newCatsToday || '--';
    document.getElementById('stat-total-feeders').textContent = stats.totalFeeders || '--';
}

function renderAlertBanner(alertCount) {
    const total = alertCount.active || 0;
    const banner = document.getElementById('alert-banner');
    const text = document.getElementById('alert-text');
    const badge = document.getElementById('alert-count-badge');
    const navBadge = document.getElementById('nav-alert-badge');

    if (total > 0) {
        let msg = '';
        if (alertCount.foodLow > 0) {
            msg += `${alertCount.foodLow}个喂养点缺粮 `;
        }
        if (alertCount.waterLow > 0) {
            msg += `${alertCount.waterLow}个缺水 `;
        }
        if (alertCount.newCat > 0) {
            msg += `${alertCount.newCat}只新猫报到`;
        }
        if (!msg) {
            msg = `${total}条待处理告警`;
        }
        text.textContent = msg;
        badge.textContent = total;
        navBadge.textContent = total;
        navBadge.style.display = total > 0 ? 'flex' : 'none';
    } else {
        text.textContent = '一切正常，棒棒哒！';
        badge.textContent = 0;
        badge.style.display = 'none';
        navBadge.style.display = 'none';
        banner.style.background = 'linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%)';
        banner.style.borderColor = '#A5D6A7';
    }
}

function renderNewCats(cats) {
    const container = document.getElementById('new-cats-list');
    if (!cats || cats.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-icon">🐾</div><div class="empty-text">暂无新猫咪</div></div>';
        return;
    }

    container.innerHTML = cats.slice(0, 5).map(cat => `
        <div class="new-cat-card" onclick="showCatDetail(${cat.id})">
            <div class="new-cat-image">
                <span class="new-cat-badge">NEW</span>
                🐱
            </div>
            <div class="new-cat-info">
                <div class="new-cat-name">${cat.name || '未知猫咪'}</div>
                <div class="new-cat-desc">${formatFurColor(cat.furColor)} · ${formatBodyType(cat.bodyType)}</div>
            </div>
        </div>
    `).join('');
}

function renderFeederListMini(feeders) {
    const container = document.getElementById('feeder-list-mini');
    if (!feeders || feeders.length === 0) {
        container.innerHTML = '<div class="loading">暂无喂养点数据</div>';
        return;
    }

    const alertFeeders = feeders.filter(f => f.foodAlert || f.waterAlert);
    const displayFeeders = alertFeeders.length > 0 ? alertFeeders : feeders.slice(0, 3);

    container.innerHTML = displayFeeders.map(feeder => {
        const hasAlert = feeder.foodAlert || feeder.waterAlert;
        const foodPercent = calculatePercent(feeder.currentFoodLevel, feeder.foodCapacity);
        const waterPercent = calculatePercent(feeder.currentWaterLevel, feeder.waterCapacity);

        return `
            <div class="feeder-card-mini" onclick="showFeederDetail('${feeder.feederCode}')">
                <div class="feeder-icon ${hasAlert ? 'alert' : ''}">${hasAlert ? '⚠️' : '🏠'}</div>
                <div class="feeder-info">
                    <div class="feeder-name">${feeder.name}</div>
                    <div class="feeder-status-text ${hasAlert ? 'alert' : ''}">
                        ${hasAlert ? getFeederAlertText(feeder) : '运行正常'}
                    </div>
                </div>
                <div class="feeder-levels">
                    <div class="level-item">
                        <span>粮</span>
                        <div class="level-bar">
                            <div class="level-fill food ${foodPercent < 20 ? 'low' : ''}" style="width: ${foodPercent}%"></div>
                        </div>
                    </div>
                    <div class="level-item">
                        <span>水</span>
                        <div class="level-bar">
                            <div class="level-fill water ${waterPercent < 20 ? 'low' : ''}" style="width: ${waterPercent}%"></div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function renderRanking(ranking) {
    const container = document.getElementById('ranking-list');
    if (!ranking || ranking.length === 0) {
        container.innerHTML = '<div class="loading">暂无排行数据</div>';
        return;
    }

    container.innerHTML = ranking.slice(0, 5).map((cat, index) => `
        <div class="ranking-item" onclick="showCatDetail(${cat.catId})">
            <div class="rank-badge rank-${index + 1}">${index + 1}</div>
            <div class="ranking-avatar">🐱</div>
            <div class="ranking-info">
                <div class="ranking-name">${cat.name}</div>
                <div class="ranking-meta">${formatLastSeen(cat.lastSeen)}</div>
            </div>
            <div class="ranking-visits">
                <div class="visits-count">${cat.visitCount}</div>
                <div class="visits-label">次到访</div>
            </div>
        </div>
    `).join('');
}

async function loadFeedersPage() {
    try {
        const result = await api.getFeeders(feederFilter).catch(() => getMockFeeders());
        renderFeederList(result.feeders || []);
    } catch (error) {
        console.error('加载喂养点失败:', error);
        loadMockFeeders();
    }
}

function filterFeeders(btn, filter) {
    document.querySelectorAll('.filter-tabs .tab-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    feederFilter = filter;
    loadFeedersPage();
}

function renderFeederList(feeders) {
    const container = document.getElementById('feeder-list');
    if (!feeders || feeders.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-icon">🏠</div><div class="empty-text">暂无喂养点数据</div></div>';
        return;
    }

    container.innerHTML = feeders.map(feeder => {
        const hasAlert = feeder.foodAlert || feeder.waterAlert;
        const foodPercent = calculatePercent(feeder.currentFoodLevel, feeder.foodCapacity);
        const waterPercent = calculatePercent(feeder.currentWaterLevel, feeder.waterCapacity);
        const status = feeder.status;

        return `
            <div class="feeder-card" onclick="showFeederDetail('${feeder.feederCode}')">
                <div class="feeder-card-header">
                    <div class="feeder-card-icon ${hasAlert ? 'alert' : ''}">${hasAlert ? '⚠️' : '🏠'}</div>
                    <div class="feeder-card-info">
                        <div class="feeder-card-name">${feeder.name}</div>
                        <div class="feeder-card-location">${feeder.location || '位置未知'}</div>
                    </div>
                    <span class="status-badge ${hasAlert ? 'alert' : status}">${hasAlert ? '需补充' : (status === 'online' ? '在线' : '离线')}</span>
                </div>
                <div class="feeder-levels-row">
                    <div class="level-box">
                        <div class="level-box-header">
                            <span class="level-box-label">🥫 粮草</span>
                            <span class="level-box-value ${foodPercent < 20 ? 'low' : ''}">${foodPercent}%</span>
                        </div>
                        <div class="level-progress">
                            <div class="level-progress-fill food ${foodPercent < 20 ? 'low' : ''}" style="width: ${foodPercent}%"></div>
                        </div>
                    </div>
                    <div class="level-box">
                        <div class="level-box-header">
                            <span class="level-box-label">💧 饮水</span>
                            <span class="level-box-value ${waterPercent < 20 ? 'low' : ''}">${waterPercent}%</span>
                        </div>
                        <div class="level-progress">
                            <div class="level-progress-fill water ${waterPercent < 20 ? 'low' : ''}" style="width: ${waterPercent}%"></div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

async function showFeederDetail(code) {
    showPage('feeder-detail');
    document.getElementById('feeder-detail-name').textContent = '加载中...';

    try {
        const [feeder, sensorData] = await Promise.all([
            api.getFeederByCode(code).catch(() => getMockFeederDetail(code)),
            api.getFeederSensorData(code).catch(() => ({ data: [] }))
        ]);

        renderFeederDetail(feeder, sensorData.data || []);
    } catch (error) {
        console.error('加载喂养点详情失败:', error);
    }
}

function renderFeederDetail(feeder, sensorData) {
    document.getElementById('feeder-detail-name').textContent = feeder.name;

    const hasAlert = feeder.foodAlert || feeder.waterAlert;
    const foodPercent = calculatePercent(feeder.currentFoodLevel, feeder.foodCapacity);
    const waterPercent = calculatePercent(feeder.currentWaterLevel, feeder.waterCapacity);

    const content = document.getElementById('feeder-detail-content');
    content.innerHTML = `
        <div class="feeder-detail-hero">
            <div class="feeder-detail-header">
                <div class="feeder-detail-icon ${hasAlert ? 'alert' : ''}">${hasAlert ? '⚠️' : '🏠'}</div>
                <div class="feeder-detail-info">
                    <div class="feeder-detail-name">${feeder.name}</div>
                    <div class="feeder-detail-location">${feeder.location || '位置未知'}</div>
                </div>
                <span class="status-badge ${hasAlert ? 'alert' : feeder.status}">${hasAlert ? '需补充' : (feeder.status === 'online' ? '在线' : '离线')}</span>
            </div>
            <div class="feeder-detail-stats">
                <div class="feeder-detail-stat">
                    <div class="feeder-detail-stat-value ${foodPercent < 20 ? 'danger' : ''}">${foodPercent}%</div>
                    <div class="feeder-detail-stat-label">粮草余量</div>
                </div>
                <div class="feeder-detail-stat">
                    <div class="feeder-detail-stat-value ${waterPercent < 20 ? 'danger' : ''}">${waterPercent}%</div>
                    <div class="feeder-detail-stat-label">饮水余量</div>
                </div>
                <div class="feeder-detail-stat">
                    <div class="feeder-detail-stat-value">${feeder.batteryLevel || '--'}%</div>
                    <div class="feeder-detail-stat-label">电量</div>
                </div>
            </div>
        </div>

        <div class="detail-section">
            <div class="detail-section-title">📊 状态详情</div>
            <div class="detail-info-grid">
                <div class="detail-info-item">
                    <span class="detail-info-label">设备编号</span>
                    <span class="detail-info-value">${feeder.feederCode}</span>
                </div>
                <div class="detail-info-item">
                    <span class="detail-info-label">粮草容量</span>
                    <span class="detail-info-value">${feeder.foodCapacity || '--'}g</span>
                </div>
                <div class="detail-info-item">
                    <span class="detail-info-label">饮水容量</span>
                    <span class="detail-info-value">${feeder.waterCapacity || '--'}ml</span>
                </div>
                <div class="detail-info-item">
                    <span class="detail-info-label">最后心跳</span>
                    <span class="detail-info-value">${formatTime(feeder.lastHeartbeat)}</span>
                </div>
            </div>
        </div>

        <div class="detail-section">
            <div class="detail-section-title">🔔 告警状态</div>
            <div class="detail-info-grid">
                <div class="detail-info-item">
                    <span class="detail-info-label">粮草告警</span>
                    <span class="detail-info-value" style="color: ${feeder.foodAlert ? 'var(--danger-color)' : 'var(--success-color)'}">
                        ${feeder.foodAlert ? '已触发' : '正常'}
                    </span>
                </div>
                <div class="detail-info-item">
                    <span class="detail-info-label">饮水告警</span>
                    <span class="detail-info-value" style="color: ${feeder.waterAlert ? 'var(--danger-color)' : 'var(--success-color)'}">
                        ${feeder.waterAlert ? '已触发' : '正常'}
                    </span>
                </div>
            </div>
        </div>

        ${sensorData && sensorData.length > 0 ? `
        <div class="detail-section">
            <div class="detail-section-title">📈 最近记录 (${sensorData.length}条)</div>
            <div style="font-size: 12px; color: var(--text-secondary);">
                最新: ${formatTime(sensorData[0].recordTime)}
            </div>
        </div>
        ` : ''}
    `;
}

async function loadCatsPage() {
    try {
        const result = await api.getCats(catFilter).catch(() => getMockCats());
        renderCatGrid(result.cats || []);
    } catch (error) {
        console.error('加载猫咪列表失败:', error);
        loadMockCats();
    }
}

function filterCats(btn, filter) {
    document.querySelectorAll('.filter-tabs .tab-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    catFilter = filter;
    loadCatsPage();
}

function renderCatGrid(cats) {
    const container = document.getElementById('cat-grid');
    if (!cats || cats.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-icon">🐱</div><div class="empty-text">暂无猫咪数据</div></div>';
        return;
    }

    container.innerHTML = cats.map(cat => `
        <div class="cat-card" onclick="showCatDetail(${cat.id})">
            <div class="cat-card-image">
                ${cat.isNew ? '<span class="cat-new-badge">NEW</span>' : ''}
                🐱
            </div>
            <div class="cat-card-info">
                <div class="cat-card-name">${cat.name || '未知猫咪'}</div>
                <div class="cat-card-meta">
                    <span>${formatFurColor(cat.furColor)}</span>
                    <span class="cat-visits">${cat.visitCount || 0}次</span>
                </div>
            </div>
        </div>
    `).join('');
}

async function showCatDetail(id) {
    showPage('cat-detail');
    document.getElementById('cat-detail-name').textContent = '加载中...';

    try {
        const [cat, captures] = await Promise.all([
            api.getCatById(id).catch(() => getMockCatDetail(id)),
            api.getCatCaptures(id).catch(() => ({ captures: [] }))
        ]);

        renderCatDetail(cat, captures.captures || []);
    } catch (error) {
        console.error('加载猫咪详情失败:', error);
    }
}

function renderCatDetail(cat, captures) {
    document.getElementById('cat-detail-name').textContent = cat.name || '未知猫咪';

    const content = document.getElementById('cat-detail-content');
    content.innerHTML = `
        <div class="detail-hero">
            <div class="detail-avatar">🐱</div>
            <div class="detail-name">${cat.name || '未知猫咪'}</div>
            <div class="detail-code">${cat.catCode || ''}</div>
        </div>

        <div class="detail-section">
            <div class="detail-section-title">📊 到访统计</div>
            <div class="detail-stats">
                <div class="detail-stat">
                    <div class="detail-stat-value">${cat.visitCount || 0}</div>
                    <div class="detail-stat-label">总到访</div>
                </div>
                <div class="detail-stat">
                    <div class="detail-stat-value">${formatTimeAgo(cat.firstSeenTime)}</div>
                    <div class="detail-stat-label">首次见面</div>
                </div>
                <div class="detail-stat">
                    <div class="detail-stat-value">${formatTimeAgo(cat.lastSeenTime)}</div>
                    <div class="detail-stat-label">最近到访</div>
                </div>
            </div>
        </div>

        <div class="detail-section">
            <div class="detail-section-title">🎨 特征档案</div>
            <div class="detail-info-grid">
                <div class="detail-info-item">
                    <span class="detail-info-label">毛色</span>
                    <span class="detail-info-value">${formatFurColor(cat.furColor)}</span>
                </div>
                <div class="detail-info-item">
                    <span class="detail-info-label">花纹</span>
                    <span class="detail-info-value">${formatFurPattern(cat.furPattern)}</span>
                </div>
                <div class="detail-info-item">
                    <span class="detail-info-label">体型</span>
                    <span class="detail-info-value">${formatBodyType(cat.bodyType)}</span>
                </div>
                <div class="detail-info-item">
                    <span class="detail-info-label">眼睛</span>
                    <span class="detail-info-value">${formatEyeColor(cat.eyeColor)}</span>
                </div>
                <div class="detail-info-item">
                    <span class="detail-info-label">性别</span>
                    <span class="detail-info-value">${formatGender(cat.gender)}</span>
                </div>
                <div class="detail-info-item">
                    <span class="detail-info-label">绝育</span>
                    <span class="detail-info-value">${cat.isNeutered ? '已绝育' : '未绝育'}</span>
                </div>
            </div>
        </div>

        ${cat.description ? `
        <div class="detail-section">
            <div class="detail-section-title">📝 简介</div>
            <p style="font-size: 13px; color: var(--text-secondary); line-height: 1.6;">${cat.description}</p>
        </div>
        ` : ''}

        ${captures.length > 0 ? `
        <div class="detail-section">
            <div class="detail-section-title">📸 抓拍记录 (${captures.length})</div>
            <div class="capture-grid">
                ${captures.slice(0, 9).map(cap => `
                    <div class="capture-item">
                        🐱
                        <span class="capture-time">${formatTimeShort(cap.captureTime)}</span>
                    </div>
                `).join('')}
            </div>
        </div>
        ` : ''}
    `;
}

async function loadAlertsPage() {
    try {
        const result = await api.getAlerts(alertFilter === 'active').catch(() => getMockAlerts());
        renderAlertList(result.alerts || []);
    } catch (error) {
        console.error('加载告警列表失败:', error);
        loadMockAlerts();
    }
}

function filterAlerts(btn, filter) {
    document.querySelectorAll('.filter-tabs .tab-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    alertFilter = filter;
    loadAlertsPage();
}

function renderAlertList(alerts) {
    const container = document.getElementById('alert-list');
    if (!alerts || alerts.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-icon">✅</div><div class="empty-text">暂无告警，一切正常</div></div>';
        return;
    }

    container.innerHTML = alerts.map(alert => {
        const typeClass = getAlertTypeClass(alert.type);
        const icon = getAlertIcon(alert.type);

        return `
            <div class="alert-card ${typeClass} ${alert.resolved ? 'resolved' : ''}">
                <div class="alert-card-icon">${icon}</div>
                <div class="alert-card-content">
                    <div class="alert-card-title">${getAlertTitle(alert.type)}</div>
                    <div class="alert-card-message">${alert.message}</div>
                    <div class="alert-card-time">${formatTime(alert.createTime)}</div>
                </div>
                ${!alert.resolved ? `
                    <div class="alert-card-action">
                        <button class="resolve-btn" onclick="resolveAlert(${alert.id}, event)">处理</button>
                    </div>
                ` : ''}
            </div>
        `;
    }).join('');
}

async function resolveAlert(id, event) {
    event.stopPropagation();
    try {
        await api.resolveAlert(id);
        loadAlertsPage();
        if (currentPage === 'home') {
            loadHomePage();
        }
    } catch (error) {
        console.error('处理告警失败:', error);
        alert('处理失败，请重试');
    }
}

function calculatePercent(current, total) {
    if (!current || !total || total === 0) return 0;
    return Math.round((current / total) * 100);
}

function formatFurColor(color) {
    const colorMap = {
        'orange': '橘色',
        'black': '黑色',
        'white': '白色',
        'gray': '灰色',
        'calico': '三花',
        'tuxedo': '奶牛',
        'black_white': '黑白',
        'tabby': '虎斑',
        'brown': '棕色'
    };
    return colorMap[color] || color || '未知';
}

function formatFurPattern(pattern) {
    const patternMap = {
        'solid': '纯色',
        'tabby': '虎斑',
        'tricolor': '三色',
        'tuxedo': '燕尾服',
        'striped': '条纹',
        'spotted': '斑点'
    };
    return patternMap[pattern] || pattern || '未知';
}

function formatBodyType(type) {
    const typeMap = {
        'slim': '苗条',
        'normal': '标准',
        'fat': '胖',
        'chubby': '圆滚滚',
        'muscular': '肌肉型'
    };
    return typeMap[type] || type || '未知';
}

function formatEyeColor(color) {
    const colorMap = {
        'yellow': '黄色',
        'green': '绿色',
        'blue': '蓝色',
        'brown': '棕色',
        'copper': '铜色',
        'odd': '异色瞳'
    };
    return colorMap[color] || color || '未知';
}

function formatGender(gender) {
    const genderMap = {
        'male': '公',
        'female': '母'
    };
    return genderMap[gender] || gender || '未知';
}

function formatTime(timeStr) {
    if (!timeStr) return '--';
    const date = new Date(timeStr);
    if (isNaN(date.getTime())) return '--';
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return '刚刚';
    if (diffMins < 60) return `${diffMins}分钟前`;
    if (diffHours < 24) return `${diffHours}小时前`;
    if (diffDays < 7) return `${diffDays}天前`;

    return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

function formatTimeShort(timeStr) {
    if (!timeStr) return '--';
    const date = new Date(timeStr);
    if (isNaN(date.getTime())) return '--';
    return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

function formatTimeAgo(timeStr) {
    if (!timeStr) return '--';
    const date = new Date(timeStr);
    if (isNaN(date.getTime())) return '--';
    const now = new Date();
    const diffMs = now - date;
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffDays === 0) return '今天';
    if (diffDays === 1) return '昨天';
    if (diffDays < 30) return `${diffDays}天前`;
    if (diffDays < 365) return `${Math.floor(diffDays / 30)}个月前`;
    return `${Math.floor(diffDays / 365)}年前`;
}

function formatLastSeen(lastSeen) {
    if (!lastSeen) return '未记录';
    return `最近到访: ${formatTime(lastSeen)}`;
}

function getFeederAlertText(feeder) {
    const alerts = [];
    if (feeder.foodAlert) alerts.push('缺粮');
    if (feeder.waterAlert) alerts.push('缺水');
    return alerts.join('、');
}

function getAlertTypeClass(type) {
    const typeMap = {
        'FOOD_LOW': 'danger',
        'WATER_LOW': 'danger',
        'NEW_CAT': 'info',
        'DEVICE_OFFLINE': 'warning',
        'BATTERY_LOW': 'warning'
    };
    return typeMap[type] || 'info';
}

function getAlertIcon(type) {
    const iconMap = {
        'FOOD_LOW': '🥫',
        'WATER_LOW': '💧',
        'NEW_CAT': '🐱',
        'DEVICE_OFFLINE': '📴',
        'BATTERY_LOW': '🔋'
    };
    return iconMap[type] || '⚠️';
}

function getAlertTitle(type) {
    const titleMap = {
        'FOOD_LOW': '粮草不足',
        'WATER_LOW': '饮水不足',
        'NEW_CAT': '新猫报到',
        'DEVICE_OFFLINE': '设备离线',
        'BATTERY_LOW': '电量低'
    };
    return titleMap[type] || '系统告警';
}

function getMockDashboardStats() {
    return {
        totalCats: 12,
        newCatsToday: 2,
        totalFeeders: 3,
        feedersWithAlerts: 2,
        capturesToday: 28,
        uniqueCatsToday: 7
    };
}

function getMockAlertCount() {
    return {
        active: 3,
        foodLow: 1,
        waterLow: 1,
        newCat: 1
    };
}

function getMockNewCats() {
    return {
        cats: [
            { id: 3, name: '三花', furColor: 'calico', bodyType: 'normal', isNew: true },
            { id: 5, name: '小花', furColor: 'tabby', bodyType: 'slim', isNew: true }
        ]
    };
}

function getMockFeeders() {
    return {
        feeders: [
            { id: 1, feederCode: 'feeder-001', name: '1号楼喂养点', location: '1号楼东侧花园', currentFoodLevel: 75, foodCapacity: 100, currentWaterLevel: 60, waterCapacity: 100, batteryLevel: 85, status: 'online', foodAlert: false, waterAlert: false },
            { id: 2, feederCode: 'feeder-002', name: '3号楼喂养点', location: '3号楼南侧草坪', currentFoodLevel: 15, foodCapacity: 100, currentWaterLevel: 45, waterCapacity: 100, batteryLevel: 70, status: 'online', foodAlert: true, waterAlert: false },
            { id: 3, feederCode: 'feeder-003', name: '5号楼喂养点', location: '5号楼北门', currentFoodLevel: 50, foodCapacity: 100, currentWaterLevel: 18, waterCapacity: 100, batteryLevel: 95, status: 'online', foodAlert: false, waterAlert: true }
        ]
    };
}

function getMockFeederDetail(code) {
    const feeders = getMockFeeders().feeders;
    return feeders.find(f => f.feederCode === code) || feeders[0];
}

function getMockCats() {
    return {
        cats: [
            { id: 1, name: '大橘', catCode: 'CAT-A1B2', furColor: 'orange', bodyType: 'fat', visitCount: 128, isNew: false, avatarUrl: '', lastSeenTime: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString() },
            { id: 2, name: '小黑', catCode: 'CAT-C3D4', furColor: 'black', bodyType: 'slim', visitCount: 56, isNew: false, avatarUrl: '', lastSeenTime: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString() },
            { id: 3, name: '三花', catCode: 'CAT-E5F6', furColor: 'calico', bodyType: 'normal', visitCount: 8, isNew: true, avatarUrl: '', lastSeenTime: new Date(Date.now() - 8 * 60 * 60 * 1000).toISOString() },
            { id: 4, name: '奶牛', catCode: 'CAT-G7H8', furColor: 'black_white', bodyType: 'normal', visitCount: 256, isNew: false, avatarUrl: '', lastSeenTime: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString() },
            { id: 5, name: '小灰', catCode: 'CAT-I9J0', furColor: 'gray', bodyType: 'normal', visitCount: 34, isNew: false, avatarUrl: '', lastSeenTime: new Date(Date.now() - 12 * 60 * 60 * 1000).toISOString() },
            { id: 6, name: '小白', catCode: 'CAT-K1L2', furColor: 'white', bodyType: 'slim', visitCount: 15, isNew: false, avatarUrl: '', lastSeenTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString() }
        ]
    };
}

function getMockCatDetail(id) {
    const cats = getMockCats().cats;
    let cat = cats.find(c => c.id == id) || cats[0];
    return {
        ...cat,
        furPattern: 'tabby',
        eyeColor: 'yellow',
        gender: 'male',
        estimatedAge: 3,
        isNeutered: true,
        description: '小区明星猫，性格温顺，特别能吃。每天大概下午3点和晚上9点会来吃饭，是个守时的小家伙。',
        firstSeenTime: new Date(Date.now() - 180 * 24 * 60 * 60 * 1000).toISOString()
    };
}

function getMockRanking() {
    return [
        { rank: 1, catId: 4, name: '奶牛', visitCount: 256, lastSeen: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString() },
        { rank: 2, catId: 1, name: '大橘', visitCount: 128, lastSeen: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString() },
        { rank: 3, catId: 2, name: '小黑', visitCount: 56, lastSeen: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString() },
        { rank: 4, catId: 5, name: '小灰', visitCount: 34, lastSeen: new Date(Date.now() - 12 * 60 * 60 * 1000).toISOString() },
        { rank: 5, catId: 6, name: '小白', visitCount: 15, lastSeen: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString() }
    };
}

function getMockAlerts() {
    return {
        alerts: [
            { id: 1, type: 'FOOD_LOW', severity: 'warning', feederName: '3号楼喂养点', message: '3号楼喂养点 粮草不足，剩余 15%，请及时补充！', createTime: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(), resolved: false },
            { id: 2, type: 'WATER_LOW', severity: 'warning', feederName: '5号楼喂养点', message: '5号楼喂养点 水量不足，剩余 18%，请及时补充！', createTime: new Date(Date.now() - 30 * 60 * 1000).toISOString(), resolved: false },
            { id: 3, type: 'NEW_CAT', severity: 'info', catName: '三花', feederName: '1号楼喂养点', message: '发现新猫咪「三花」在 1号楼喂养点 出没！', createTime: new Date(Date.now() - 4 * 60 * 60 * 1000).toISOString(), resolved: false },
            { id: 4, type: 'FOOD_LOW', severity: 'warning', feederName: '2号楼喂养点', message: '2号楼喂养点 粮草已补充，告警解除', createTime: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(), resolved: true }
        ]
    };
}

function loadMockHomeData() {
    const stats = getMockDashboardStats();
    const alertCount = getMockAlertCount();
    const newCats = getMockNewCats();
    const feeders = getMockFeeders();
    const ranking = getMockRanking();

    renderDashboardStats(stats);
    renderAlertBanner(alertCount);
    renderNewCats(newCats.cats);
    renderFeederListMini(feeders.feeders);
    renderRanking(ranking);
}

function loadMockFeeders() {
    const result = getMockFeeders();
    renderFeederList(result.feeders);
}

function loadMockCats() {
    const result = getMockCats();
    renderCatGrid(result.cats);
}

function loadMockAlerts() {
    const result = getMockAlerts();
    renderAlertList(result.alerts);
}

document.addEventListener('DOMContentLoaded', () => {
    showPage('home');
});
