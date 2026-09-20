package com.example.graphqlshop.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/admin/products")
    public String products() {
        return "products";
    }

    @GetMapping("/admin/categories")
    public String categories() {
        return "categories";
    }
}
