let currentUserId;
let otherUserId;
let authToken;
let channel;

function startChat() {
  currentUserId = parseInt(document.getElementById("userId").value);
  otherUserId = parseInt(document.getElementById("chatWithId").value);
  authToken = document.getElementById("token").value;

  if (!currentUserId || !otherUserId || !authToken) {
    alert("Please enter your ID, token, and chat partner's ID.");
    return;
  }

  const channelId = `private-chat-${Math.min(currentUserId, otherUserId)}-${Math.max(currentUserId, otherUserId)}`;

  const pusher = new Pusher("d044493efec8a33cec65", {
    cluster: "ap2",
    auth: {
      headers: {
        Authorization: `Bearer ${authToken}`,
      }
    },
    authorizer: (channel, options) => {
      return {
        authorize: (socketId, callback) => {
          fetch("http://localhost:8081/api/chat/auth", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${authToken}`,
            },
            body: JSON.stringify({
              socket_id: socketId,
              channel_name: channel.name
            }),
          })
            .then(res => {
              if (!res.ok) throw new Error("Auth failed");
              return res.json();
            })
            .then(data => {
              console.log("✅ Auth successful", data);
              callback(null, data);
            })
            .catch(err => {
              console.error("❌ Auth error", err);
              callback(err, null);
            });
        }
      };
    }
  });

  pusher.connection.bind('connected', () => console.log('✅ Pusher connected'));
  pusher.connection.bind('error', (err) => console.error('❌ Pusher connection error:', err));

  channel = pusher.subscribe(channelId);

  channel.bind('subscription_succeeded', () => {
    console.log(`✅ Subscribed to channel: ${channelId}`);
  });
  channel.bind('subscription_error', (status) => {
    console.error(`❌ Subscription error with status: ${status}`);
  });

  channel.bind("new-message", function (data) {
    console.log("📨 Received message:", data);

    const messagesDiv = document.getElementById("messages");
    const msg = document.createElement("div");

    // Format sender and message clearly
    msg.innerText = `User ${data.senderId}: ${data.message}`;
    messagesDiv.appendChild(msg);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
  });

  document.getElementById("chat-box").style.display = "block";
}

function sendMessage() {
  const input = document.getElementById("messageInput");
  const message = input.value.trim();
  if (!message) return;

  fetch("http://localhost:8081/api/chat/sendMessage", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${authToken}`,
    },
    body: JSON.stringify({
      content: message,
      receiverId: otherUserId.toString()
    })
  })
    .then(res => {
      if (!res.ok) throw new Error("Send message failed");
      return res.json();
    })
    .then(() => {
      console.log("✅ Message sent");
      input.value = "";

      // Optionally show the sender's message immediately:
      const messagesDiv = document.getElementById("messages");
      const msg = document.createElement("div");
      msg.innerText = `You: ${message}`;
      messagesDiv.appendChild(msg);
      messagesDiv.scrollTop = messagesDiv.scrollHeight;
    })
    .catch(err => {
      console.error("❌ Send failed", err);
      alert("Failed to send message. Check console for details.");
    });
}
