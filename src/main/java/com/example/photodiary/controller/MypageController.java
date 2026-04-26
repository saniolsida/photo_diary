package com.example.photodiary.controller;

import com.example.photodiary.model.PrintOrder;
import com.example.photodiary.repository.PrintOrderRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final PrintOrderRepository printOrderRepository;

    @GetMapping
    public String mypage(HttpSession session, Model model) {
        String name = (String) session.getAttribute("mypageName");
        if (name == null) {
            return "mypage/login";
        }
        List<PrintOrder> orders = printOrderRepository.findByReceiverName(name);
        model.addAttribute("name", name);
        model.addAttribute("orders", orders);
        return "mypage/my_orders";
    }

    @PostMapping("/login")
    public String login(@RequestParam("name") String name, HttpSession session) {
        session.setAttribute("mypageName", name.trim());
        return "redirect:/mypage";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("mypageName");
        return "redirect:/mypage";
    }
}
