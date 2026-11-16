
const API = '/api/tasks';
let currentFilter = 'all';

function getCsrfToken() {
    const match = document.cookie.match(new RegExp('(^|;\\s*)XSRF-TOKEN=([^;]+)'));
    return match ? decodeURIComponent(match[2]) : null;
}

async function apiFetch(url, options = {}) {
    const csrfToken = getCsrfToken();
    const headers = {
        ...(options.headers || {})
    };
    if (csrfToken && options.method && options.method.toUpperCase() !== 'GET') {
        headers['X-XSRF-TOKEN'] = csrfToken;
    }

    const res = await fetch(url, { ...options, headers });
    if (res.status === 401 || res.status === 403) {
        window.location.href = '/login';
    }
    return res;
}

const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => document.querySelectorAll(sel);

const TYPE_ICONS = {
    EXAM: '📝', ASSIGNMENT: '📄', PROJECT: '🛠️', PLACEMENT_TEST: '🎯'
};

const TYPE_LABELS = {
    EXAM: 'Exam', ASSIGNMENT: 'Assignment',
    PROJECT: 'Project', PLACEMENT_TEST: 'Placement Test'
};

function urgencyColor(label) {
    const map = {
        OVERDUE: 'var(--overdue)', 'DUE TODAY': 'var(--due-today)',
        URGENT: 'var(--urgent)', HIGH: 'var(--high)',
        MEDIUM: 'var(--medium)', LOW: 'var(--low)'
    };
    return map[label] || 'var(--border)';
}

function formatDate(dueDate) {
    if (dueDate == null || dueDate === '') return '';

    const options = { day: 'numeric', month: 'short', year: 'numeric' };

    if (typeof dueDate === 'number' ||
        (typeof dueDate === 'string' && /^\d+$/.test(dueDate))) {
        return new Date(Number(dueDate)).toLocaleDateString('en-IN', options);
    }

    const iso = String(dueDate).slice(0, 10);
    return new Date(iso + 'T00:00:00').toLocaleDateString('en-IN', options);
}

// ── Tab Switching ──────────────────────────────────────────
$$('.portal-tabs .tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        $$('.portal-tabs .tab-btn').forEach(b => b.classList.remove('active'));
        $$('.tab-pane').forEach(p => p.classList.remove('active'));

        btn.classList.add('active');
        const targetId = btn.getAttribute('data-tab');
        const pane = document.getElementById(targetId);
        if (pane) pane.classList.add('active');
    });
});

// ── UniVerse Tasks API Calls ───────────────────────────────

async function fetchPrioritized() {
    const res = await apiFetch(`${API}/prioritized`);
    if (!res.ok) throw new Error(`Could not load priorities (${res.status})`);
    return res.json();
}

async function fetchAll() {
    const res = await apiFetch(API);
    if (!res.ok) throw new Error(`Could not load tasks (${res.status})`);
    return res.json();
}

async function createTask(task) {
    const res = await apiFetch(API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(task)
    });
    if (!res.ok) throw new Error(`Could not create task (${res.status})`);
}

async function updateTask(id, task) {
    const res = await apiFetch(`${API}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(task)
    });
    if (!res.ok) throw new Error(`Could not update task (${res.status})`);
}

async function markComplete(id) {
    const res = await apiFetch(`${API}/${id}/complete`, { method: 'PATCH' });
    if (!res.ok) throw new Error(`Could not complete task (${res.status})`);
}

async function deleteTask(id) {
    const res = await apiFetch(`${API}/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error(`Could not delete task (${res.status})`);
}

// ── Rendering Tasks ────────────────────────────────────────

function renderTaskItem(entry, rank, isTop) {
    const { task, priorityScore, daysRemaining, urgencyLabel } = entry;
    const badgeClass = `badge-${urgencyLabel.replace(' ', '-')}`;
    const typeIcon = TYPE_ICONS[task.taskType] || '📋';
    const typeLabel = TYPE_LABELS[task.taskType] || task.taskType;

    const daysText = daysRemaining < 0
        ? `${Math.abs(daysRemaining)} days overdue`
        : daysRemaining === 0
            ? 'Due today!'
            : `${daysRemaining} day${daysRemaining !== 1 ? 's' : ''} left`;

    return `
        <div class="task-item ${isTop ? 'top-priority' : ''}" data-type="${task.taskType}">
            <div class="priority-bar" style="background:${urgencyColor(urgencyLabel)}"></div>
            <div class="task-rank">#${rank}</div>
            <div class="task-body">
                <div class="task-title">${task.subject} — ${task.title}</div>
                <div class="task-meta">
                    <span class="type-badge">${typeIcon} ${typeLabel}</span>
                    <span>📅 ${formatDate(task.dueDate)}</span>
                    <span>⏳ ${daysText}</span>
                    <span class="badge ${badgeClass}">${urgencyLabel}</span>
                    <span>⚡ Priority: ${priorityScore}</span>
                </div>
                ${task.description ? `<div class="task-desc">${task.description}</div>` : ''}
            </div>
            <div class="task-actions">
                <button class="btn-icon" title="Mark complete" onclick="handleComplete(${task.id})">✅</button>
                <button class="btn-icon" title="Edit" onclick="openEditModal(${task.id})">✏️</button>
                <button class="btn-icon" title="Delete" onclick="handleDelete(${task.id})">🗑️</button>
            </div>
        </div>
    `;
}

function renderCompletedItem(task) {
    return `
        <div class="task-item completed">
            <div class="task-body">
                <div class="task-title">${task.subject} — ${task.title}</div>
                <div class="task-meta">
                    <span class="type-badge">${TYPE_ICONS[task.taskType] || '📋'} ${TYPE_LABELS[task.taskType] || task.taskType}</span>
                    <span>📅 ${formatDate(task.dueDate)}</span>
                    <span class="badge badge-DONE">Completed</span>
                </div>
            </div>
            <div class="task-actions">
                <button class="btn-icon" title="Delete" onclick="handleDelete(${task.id})">🗑️</button>
            </div>
        </div>
    `;
}

async function loadDashboard() {
    const taskListEl = $('#taskList');
    const completedEl = $('#completedList');
    if (!taskListEl) return;

    try {
        const [prioritized, allTasks] = await Promise.all([
            fetchPrioritized(), fetchAll()
        ]);

        const ordered = Array.isArray(prioritized) ? prioritized.slice() : [];
        const filtered = currentFilter === 'all'
            ? ordered
            : ordered.filter(e => e.task.taskType === currentFilter);

        if (filtered.length === 0) {
            taskListEl.innerHTML = '<p class="empty-state">No pending tasks. Add your first deadline above! 🎉</p>';
        } else {
            taskListEl.innerHTML = filtered
                .map((entry, i) => renderTaskItem(entry, i + 1, i === 0))
                .join('');
        }

        if (completedEl) {
            const completed = allTasks.filter(t => t.completed);
            if (completed.length === 0) {
                completedEl.innerHTML = '<p class="empty-state">No completed tasks yet.</p>';
            } else {
                completedEl.innerHTML = completed.map(renderCompletedItem).join('');
            }
        }
    } catch (error) {
        console.error(error);
        taskListEl.innerHTML = '<p class="empty-state">Could not load tasks. Please refresh the page.</p>';
    }
}

// ── Event Handlers ─────────────────────────────────────────

const taskForm = $('#taskForm');
if (taskForm) {
    taskForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const task = {
            subject: $('#subject').value.trim(),
            title: $('#title').value.trim(),
            taskType: $('#taskType').value,
            dueDate: $('#dueDate').value,
            description: $('#description').value.trim()
        };

        try {
            await createTask(task);
            e.target.reset();
            await loadDashboard();
        } catch (error) {
            alert(`Unable to save task: ${error.message}`);
        }
    });
}

$$('.filter-group .btn-sm').forEach(btn => {
    btn.addEventListener('click', () => {
        $$('.filter-group .btn-sm').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentFilter = btn.dataset.filter;
        loadDashboard().catch(error => alert(error.message));
    });
});

async function handleComplete(id) {
    try {
        await markComplete(id);
        await loadDashboard();
    } catch (error) {
        alert(error.message);
    }
}

async function handleDelete(id) {
    if (confirm('Delete this task?')) {
        try {
            await deleteTask(id);
            await loadDashboard();
        } catch (error) {
            alert(error.message);
        }
    }
}

// ── Edit Modal ─────────────────────────────────────────────

async function openEditModal(id) {
    try {
        const res = await apiFetch(`${API}/${id}`);
        if (!res.ok) throw new Error(`Could not load task (${res.status})`);

        const task = await res.json();
        $('#editId').value = task.id;
        $('#editSubject').value = task.subject;
        $('#editTitle').value = task.title;
        $('#editTaskType').value = task.taskType;
        $('#editDueDate').value = task.dueDate;
        $('#editDescription').value = task.description || '';
        $('#editModal').classList.remove('hidden');
    } catch (error) {
        alert(error.message);
    }
}

function closeEditModal() {
    $('#editModal').classList.add('hidden');
}

const editForm = $('#editForm');
if (editForm) {
    editForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const id = $('#editId').value;
        const task = {
            subject: $('#editSubject').value.trim(),
            title: $('#editTitle').value.trim(),
            taskType: $('#editTaskType').value,
            dueDate: $('#editDueDate').value,
            description: $('#editDescription').value.trim()
        };

        try {
            await updateTask(id, task);
            closeEditModal();
            await loadDashboard();
        } catch (error) {
            alert(`Unable to update task: ${error.message}`);
        }
    });
}

const editModal = $('#editModal');
if (editModal) {
    editModal.addEventListener('click', (e) => {
        if (e.target === editModal) closeEditModal();
    });
}

// ── Workshop Registration & Academic Support Handlers ──────

window.handleWorkshopRegister = function(workshopName) {
    alert(`🎉 Successfully registered for workshop:\n"${workshopName}"\n\nA confirmation has been linked to your student account.`);
};

window.handleSupportSubmit = function(e) {
    e.preventDefault();
    const course = $('#supportCourse').value.trim();
    const type = $('#supportType').value;
    const topic = $('#supportTopic').value.trim();

    const list = $('#supportRequestsList');
    if (list) {
        const newCard = document.createElement('div');
        newCard.className = 'task-item';
        newCard.innerHTML = `
            <div class="task-body">
                <div class="task-title">${course} — ${topic.substring(0, 50)}${topic.length > 50 ? '...' : ''}</div>
                <div class="task-meta">
                    <span class="type-badge">❓ ${type}</span>
                    <span>📅 Submitted Just Now</span>
                    <span class="badge badge-PENDING">Reviewing</span>
                </div>
                <div class="task-desc">${topic}</div>
            </div>
        `;
        list.prepend(newCard);
    }

    alert('Your academic support request has been submitted to faculty!');
    e.target.reset();
};

// ── Logout form CSRF ───────────────────────────────────────
const logoutForm = $('#logoutForm');
if (logoutForm) {
    logoutForm.addEventListener('submit', () => {
        const token = getCsrfToken();
        if (token && !logoutForm.querySelector('input[name="_csrf"]')) {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = '_csrf';
            input.value = token;
            logoutForm.appendChild(input);
        }
    });
}

// ── Initialize Dashboard ───────────────────────────────────
loadDashboard();
