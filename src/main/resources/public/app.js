const eventsBox = document.getElementById('events');
const idInput = document.getElementById('entity-id');
const addBtn = document.getElementById('add-btn');
const removeBtn = document.getElementById('remove-btn');

const appendEvent = (line) => {
  eventsBox.textContent += `${line}\n`;
};

const socketProtocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
const socket = new WebSocket(`${socketProtocol}://${window.location.host}/entity`);

socket.onopen = () => appendEvent('[socket] connected');
socket.onclose = () => appendEvent('[socket] closed');
socket.onerror = () => appendEvent('[socket] error');
socket.onmessage = (event) => appendEvent(event.data);

const entityId = () => Number.parseInt(idInput.value, 10);

addBtn.addEventListener('click', async () => {
  const id = entityId();
  const response = await fetch('/entity', {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ id })
  });
  appendEvent(`[POST /entity] ${response.status}`);
});

removeBtn.addEventListener('click', async () => {
  const id = entityId();
  const response = await fetch(`/entity/${id}`, {
    method: 'DELETE'
  });
  appendEvent(`[DELETE /entity/${id}] ${response.status}`);
});
