package com.library.config;

import com.library.dao.BookDao;
import com.library.dao.BookDaoImpl;
import com.library.dao.BookRequestDao;
import com.library.dao.BookRequestDaoImpl;
import com.library.dao.UserDao;
import com.library.dao.UserDaoImpl;
import com.library.service.BookService;
import com.library.service.BookServiceImpl;
import com.library.service.RequestService;
import com.library.service.RequestServiceImpl;
import com.library.service.UserService;
import com.library.service.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Root application context. Declares the business-layer beans (DAOs and services)
 * and wires their dependencies through constructor injection. This context is shared
 * across the whole application and is separate from the web (MVC) context.
 */
@Configuration
public class RootConfig {

    /**
     * Creates the user DAO bean via the DAO factory (Factory Method pattern).
     *
     * @return a {@link UserDao} implementation
     */
    @Bean
    public UserDao userDao() {
        return com.library.dao.DaoFactory.getInstance().createUserDao();
    }

    /**
     * Creates the book DAO bean via the DAO factory.
     *
     * @return a {@link BookDao} implementation
     */
    @Bean
    public BookDao bookDao() {
        return com.library.dao.DaoFactory.getInstance().createBookDao();
    }

    /**
     * Creates the user service, injecting the user DAO.
     *
     * @param userDao the user DAO bean
     * @return a {@link UserService} implementation
     */
    @Bean
    public UserService userService(UserDao userDao) {
        return new UserServiceImpl(userDao);
    }

    /**
     * Creates the book service, injecting the book DAO.
     *
     * @param bookDao the book DAO bean
     * @return a {@link BookService} implementation
     */
    @Bean
    public BookService bookService(BookDao bookDao) {
        return new BookServiceImpl(bookDao);
    }

    /**
     * Creates the book-request DAO bean.
     *
     * @return a {@link BookRequestDao} implementation
     */
    @Bean
    public BookRequestDao bookRequestDao() {
        return com.library.dao.DaoFactory.getInstance().createBookRequestDao();
    }

    /**
     * Creates the request service, injecting the request DAO.
     *
     * @param bookRequestDao the request DAO bean
     * @return a {@link RequestService} implementation
     */
    @Bean
    public RequestService requestService(BookRequestDao bookRequestDao) {
        return new RequestServiceImpl(bookRequestDao);
    }
}