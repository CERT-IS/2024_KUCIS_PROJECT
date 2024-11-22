class websocket {
    constructor(url) {
        this.url = url;
        this.socket = null;
        this.isConnected = false;
        this.messageHandlers = [];
    }
    connect() {
        if (!this.socket || this.socket.readyState === WebSocket.CLOSED) {
            this.socket = new WebSocket(this.url);
            this.socket.onopen = () => {
                this.isConnected = true;
                console.log('Connected to WebSocket');
            };
            this.socket.onmessage = (event) => {
                this.messageHandlers.forEach(handler => handler(event));
            };
            this.socket.onerror = (error) => {
                console.error('WebSocket error:', error);
            };
            this.socket.onclose = () => {
                this.isConnected = false;
                console.log('WebSocket connection closed');
            };
        }
    }
    send(message) {
        if (this.isConnected) {
            this.socket.send(JSON.stringify(message));
        }
    }
    close() {
        if (this.socket) {
            this.socket.close();
        }
    }
    onMessage(handler) {
        this.messageHandlers.push(handler);
    }
    reconnect() {
        if (!this.isConnected) {
            console.log('Reconnecting...');
            this.connect();
        }
    }
}
const wsManager = new websocket('ws://localhost:8080/ws');
export default wsManager;