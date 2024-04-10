const dgram = require("dgram");
const ip = require("ip");
const { HTTP_PORT } = require("./constants");

const client = dgram.createSocket("udp4");
const message = Buffer.from(
  JSON.stringify({
    port: HTTP_PORT,
  })
);

client.bind(function () {
  client.setBroadcast(true);

  const PORT = 41234;
  client.send(
    message,
    0,
    message.length,
    PORT,
    "255.255.255.255",
    function (err, bytes) {
      if (err) throw err;
      console.log("UDP message sent");
      client.close();
    }
  );
});
