const dgram = require("dgram");
const server = dgram.createSocket("udp4");
const http = require("http");

const nodes = [];

server.on("error", (err) => {
  console.log(`Server error:\n${err.stack}`);
  server.close();
});

server.on("message", (msg, rinfo) => {
  console.log(`Server got: ${msg} from ${rinfo.address}:${rinfo.port}`);

  const info = JSON.parse(msg);
  nodes.push({
    port: info.port,
    address: rinfo.address,
  });

  const postData = JSON.stringify({ message: "New node discovered" });

  const options = {
    hostname: rinfo.address,
    port: info.port,
    path: "/",
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Content-Length": Buffer.byteLength(postData),
    },
  };

  const req = http.request(options, (res) => {
    console.log(`STATUS: ${res.statusCode}`);
    console.log(`HEADERS: ${JSON.stringify(res.headers)}`);
    res.setEncoding("utf8");
    res.on("data", (chunk) => {
      console.log(`BODY: ${chunk}`);
    });
    res.on("end", () => {
      console.log("No more data in response.");
    });
  });

  req.on("error", (e) => {
    console.error(`problem with request: ${e.message}`);
  });

  // Write data to request body
  req.write(postData);
  req.end();
});

server.on("listening", () => {
  const address = server.address();
  console.log(`Server listening ${address.address}:${address.port}`);
});

// Listen on port 41234
const PORT = 41234;
server.bind(PORT, () => {
  console.log(`Server listening on port ${PORT}`);
  server.setBroadcast(true);
});

// Set the server to listen for broadcast messages
