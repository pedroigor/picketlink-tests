package org.picketlink.test.authentication.web.module;

import org.picketlink.Identity;
import org.picketlink.idm.model.basic.User;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = "/*")
public class ProtectedServlet extends HttpServlet {

    @Inject
    private Identity identity;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();

        if (this.identity.isLoggedIn()) {
            User user = (User) this.identity.getAccount();

            writer.print("Authenticated user is: " + user.getLoginName());
        }

        writer.close();
    }
}