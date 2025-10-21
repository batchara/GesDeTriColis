package com.raoudate.GestionDeTri.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/superviseur")

public class SuperviseurController {

    @GetMapping
    public String get(){
        return " GET :: superviseur controller";
    }
    @PostMapping
    public String post() {
        return " POST :: superviseur controller";
    }

    @PutMapping
    public String put() {
        return " PUT :: superviseur controller";
    }

    @DeleteMapping
    public String delete() {
        return " DELETE :: superviseur controller";
    }
}
