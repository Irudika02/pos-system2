package com.devstack.pos.server;

import com.devstack.pos.bo.BOFactory;
import com.devstack.pos.bo.custom.CustomerBO;
import com.devstack.pos.bo.custom.OrderBO;
import com.devstack.pos.bo.custom.ProductBO;
import com.devstack.pos.bo.custom.UserBO;
import com.devstack.pos.dto.CartItemDTO;
import com.devstack.pos.dto.CustomerDTO;
import com.devstack.pos.dto.OrderDTO;
import com.devstack.pos.dto.ProductDTO;
import com.devstack.pos.dto.request.RequestUserDTO;
import com.devstack.pos.dto.response.ResponseUserDTO;
import com.devstack.pos.util.BoType;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public class MobilePosServer {
    private static HttpServer server;
    private static final int PORT = 8080;

    private static UserBO userBO = BOFactory.getInstance().getBo(BoType.USER);
    private static CustomerBO customerBO = BOFactory.getInstance().getBo(BoType.CUSTOMER);
    private static ProductBO productBO = BOFactory.getInstance().getBo(BoType.PRODUCT);
    private static OrderBO orderBO = BOFactory.getInstance().getBo(BoType.ORDER);

    public static void startServer() {
        if (server != null) return;
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/api/login", new LoginHandler());
            server.createContext("/api/register", new RegisterHandler());
            server.createContext("/api/customers", new CustomerHandler());
            server.createContext("/api/products", new ProductHandler());
            server.createContext("/api/orders", new OrderHandler());
            server.createContext("/api/stats", new StatsHandler());
            server.createContext("/", new StaticFileHandler());
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();

            String ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("=================================================");
            System.out.println("🚀 Mobile POS Server is running!");
            System.out.println("📱 iPhone Access URL: http://" + ip + ":" + PORT);
            System.out.println("💻 Local Host URL:   http://localhost:" + PORT);
            System.out.println("=================================================");
        } catch (Exception e) {
            System.err.println("Failed to start Mobile POS Server: " + e.getMessage());
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
            if (kv.length == 2) {
                String k = kv[0].trim().replace("\"", "");
                String v = kv[1].trim().replace("\"", "");
                map.put(k, v);
            }
        }
        return map;
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, "{}");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }
            try {
                String body = readRequestBody(exchange);
                Map<String, String> json = parseJson(body);
                String email = json.get("email");
                String password = json.get("password");

                ResponseUserDTO res = userBO.loginUser(email, password);
                if (res.isStatus()) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"msg\":\"" + res.getMsg() + "\",\"name\":\"" + res.getDisplayName() + "\"}");
                } else {
                    sendJsonResponse(exchange, 401, "{\"success\":false,\"msg\":\"" + res.getMsg() + "\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"msg\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, "{}");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }
            try {
                String body = readRequestBody(exchange);
                Map<String, String> json = parseJson(body);
                RequestUserDTO user = new RequestUserDTO(
                        json.get("email"),
                        json.get("displayName"),
                        json.get("contactNumber"),
                        json.get("password")
                );
                boolean saved = userBO.registerUser(user);
                if (saved) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"msg\":\"User registered successfully!\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"msg\":\"Registration failed!\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"msg\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class CustomerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, "{}");
                return;
            }
            String method = exchange.getRequestMethod();
            try {
                if ("GET".equalsIgnoreCase(method)) {
                    List<CustomerDTO> list = customerBO.getAllCustomers();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        CustomerDTO c = list.get(i);
                        sb.append(String.format("{\"id\":\"%s\",\"name\":\"%s\",\"address\":\"%s\",\"salary\":%.2f}",
                                c.getCustomerId(), c.getName(), c.getAddress(), c.getSalary()));
                        if (i < list.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    sendJsonResponse(exchange, 200, sb.toString());
                } else if ("POST".equalsIgnoreCase(method)) {
                    String body = readRequestBody(exchange);
                    Map<String, String> json = parseJson(body);
                    String id = json.getOrDefault("id", "C-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
                    String name = json.get("name");
                    String address = json.get("address");
                    double salary = Double.parseDouble(json.getOrDefault("salary", "0.0"));

                    CustomerDTO dto = new CustomerDTO(id, name, address, salary);
                    boolean saved = customerBO.saveCustomer(dto);
                    if (saved) {
                        sendJsonResponse(exchange, 200, "{\"success\":true,\"msg\":\"Customer saved!\",\"id\":\"" + id + "\"}");
                    } else {
                        sendJsonResponse(exchange, 400, "{\"success\":false,\"msg\":\"Failed to save customer\"}");
                    }
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class ProductHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, "{}");
                return;
            }
            String method = exchange.getRequestMethod();
            try {
                if ("GET".equalsIgnoreCase(method)) {
                    List<ProductDTO> list = productBO.getAllProducts();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        ProductDTO p = list.get(i);
                        sb.append(String.format("{\"code\":\"%s\",\"description\":\"%s\",\"unitPrice\":%.2f,\"qtyOnHand\":%d,\"qrCode\":\"%s\"}",
                                p.getCode(), p.getDescription(), p.getUnitPrice(), p.getQtyOnHand(), p.getQrCode()));
                        if (i < list.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    sendJsonResponse(exchange, 200, sb.toString());
                } else if ("POST".equalsIgnoreCase(method)) {
                    String body = readRequestBody(exchange);
                    Map<String, String> json = parseJson(body);
                    String code = json.getOrDefault("code", "P-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
                    String desc = json.get("description");
                    double price = Double.parseDouble(json.getOrDefault("unitPrice", "0.0"));
                    int qty = Integer.parseInt(json.getOrDefault("qtyOnHand", "0"));

                    ProductDTO dto = new ProductDTO(code, desc, price, qty, "QR-" + code);
                    boolean saved = productBO.saveProduct(dto);
                    if (saved) {
                        sendJsonResponse(exchange, 200, "{\"success\":true,\"msg\":\"Product saved!\",\"code\":\"" + code + "\"}");
                    } else {
                        sendJsonResponse(exchange, 400, "{\"success\":false,\"msg\":\"Failed to save product\"}");
                    }
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, "{}");
                return;
            }
            String method = exchange.getRequestMethod();
            try {
                if ("GET".equalsIgnoreCase(method)) {
                    List<OrderDTO> list = orderBO.getAllOrders();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        OrderDTO o = list.get(i);
                        sb.append(String.format("{\"orderId\":\"%s\",\"date\":\"%s\",\"totalCost\":%.2f,\"customerId\":\"%s\",\"userEmail\":\"%s\"}",
                                o.getOrderId(), o.getDate(), o.getTotalCost(), o.getCustomerId(), o.getUserEmail()));
                        if (i < list.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    sendJsonResponse(exchange, 200, sb.toString());
                } else if ("POST".equalsIgnoreCase(method)) {
                    String body = readRequestBody(exchange);
                    Map<String, String> json = parseJson(body);
                    String customerId = json.get("customerId");
                    String userEmail = json.getOrDefault("userEmail", "mobile@pos.com");
                    String itemsJson = json.get("items");

                    String orderId = orderBO.generateNextOrderId();
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                    List<CartItemDTO> items = new ArrayList<>();
                    double grandTotal = 0;

                    if (itemsJson != null && !itemsJson.isEmpty()) {
                        String[] itemArr = itemsJson.split(";");
                        for (String itemStr : itemArr) {
                            String[] parts = itemStr.split("\\|");
                            if (parts.length >= 4) {
                                String code = parts[0];
                                String desc = parts[1];
                                double price = Double.parseDouble(parts[2]);
                                int qty = Integer.parseInt(parts[3]);
                                double itemTotal = price * qty;
                                grandTotal += itemTotal;
                                items.add(new CartItemDTO(code, desc, price, qty, itemTotal));
                            }
                        }
                    }

                    OrderDTO orderDTO = new OrderDTO(orderId, date, grandTotal, customerId, userEmail, items);
                    boolean isPlaced = orderBO.placeOrder(orderDTO);

                    if (isPlaced) {
                        sendJsonResponse(exchange, 200, String.format("{\"success\":true,\"msg\":\"Order placed successfully!\",\"orderId\":\"%s\",\"total\":%.2f}", orderId, grandTotal));
                    } else {
                        sendJsonResponse(exchange, 400, "{\"success\":false,\"msg\":\"Failed to place order\"}");
                    }
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, "{}");
                return;
            }
            try {
                int customers = customerBO.getAllCustomers().size();
                int products = productBO.getAllProducts().size();
                List<OrderDTO> orders = orderBO.getAllOrders();
                int orderCount = orders.size();
                double totalRev = 0;
                for (OrderDTO o : orders) {
                    totalRev += o.getTotalCost();
                }
                String json = String.format("{\"customers\":%d,\"products\":%d,\"orders\":%d,\"revenue\":%.2f}",
                        customers, products, orderCount, totalRev);
                sendJsonResponse(exchange, 200, json);
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }
            File file = new File("web" + path);
            if (!file.exists() || file.isDirectory()) {
                file = new File("web/index.html");
            }
            if (!file.exists()) {
                String error = "<html><body><h1>Mobile POS Server Running</h1><p>Web files loading...</p></body></html>";
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, error.length());
                OutputStream os = exchange.getResponseBody();
                os.write(error.getBytes(StandardCharsets.UTF_8));
                os.close();
                return;
            }

            String contentType = "text/html";
            if (path.endsWith(".css")) contentType = "text/css";
            else if (path.endsWith(".js")) contentType = "application/javascript";
            else if (path.endsWith(".json")) contentType = "application/json";
            else if (path.endsWith(".png")) contentType = "image/png";
            else if (path.endsWith(".jpg")) contentType = "image/jpeg";
            else if (path.endsWith(".svg")) contentType = "image/svg+xml";

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}
