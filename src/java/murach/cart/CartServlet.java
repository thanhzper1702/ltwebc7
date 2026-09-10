package murach.cart;

import murach.data.ProductIO;
import murach.business.LineItem;
import murach.business.Cart;
import murach.business.Product;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.ArrayList;

public class CartServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String url = "/index.jsp";
        ServletContext sc = getServletContext();

        String action = request.getParameter("action");
        if (action == null) {
            action = "cart";
        }

        if (action.equals("shop")) {
            url = "/index.jsp";
        } else if (action.equals("checkout")) {
            url = "/checkout.jsp";
        } else {
            String productCode = request.getParameter("productCode");
            String quantityString = request.getParameter("quantity");

            HttpSession session = request.getSession();
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart == null) {
                cart = new Cart();
            }

            int quantity = 1;
            try {
                if (quantityString != null && !quantityString.trim().isEmpty()) {
                    quantity = Integer.parseInt(quantityString);
                    if (quantity < 0) {
                        quantity = 1;
                    }
                }
            } catch (NumberFormatException nfe) {
                quantity = 1;
            }

            if (productCode != null && !productCode.trim().isEmpty()) {
                String path = sc.getRealPath("/WEB-INF/products.txt");
                Product product = ProductIO.getProduct(productCode, path);

                System.out.println("=== DEBUG CART ===");
                System.out.println("Path: " + path);
                System.out.println("ProductCode: " + productCode);
                System.out.println("Product: " + product);

                // CHỈ XỬ LÝ KHI PRODUCT TÌM THẤY HỢP LỆ
                if (product != null) {
                    ArrayList<LineItem> items = cart.getItems();
                    boolean found = false;

                    if (items != null) {
                        for (LineItem item : items) {
                            if (item.getProduct() != null &&
                                    item.getProduct().getCode().equalsIgnoreCase(product.getCode())) {
                                found = true;
                                if (quantity > 0) {
                                    if (action.equals("cart")) {
                                        item.setQuantity(quantity);
                                    } else if (action.equals("add")) {
                                        item.increaseQuantity(quantity);
                                    }
                                } else {
                                    cart.removeItem(item);
                                }
                                break;
                            }
                        }
                    }

                    if (!found && quantity > 0) {
                        LineItem item = new LineItem();
                        item.setProduct(product);
                        item.setQuantity(quantity);
                        cart.addItem(item);
                    }
                } else {
                    System.err.println("WARNING: Khong tim thay thong tin san pham trong products.txt!");
                }

                session.setAttribute("cart", cart);
            }

            url = "/cart.jsp";
        }

        sc.getRequestDispatcher(url).forward(request, response);
    }
}