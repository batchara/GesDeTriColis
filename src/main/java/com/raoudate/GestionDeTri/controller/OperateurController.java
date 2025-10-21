package com.raoudate.GestionDeTri.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/operateur")

public class OperateurController {


        @GetMapping
        public String get(){
            return " GET :: Operateur controller";
        }
        @PostMapping
        public String post() {
            return " POST :: Operateur controller";
        }

        @PutMapping
        public String put() {
            return " PUT :: Operateur controller";
        }

        @DeleteMapping
        public String delete() {
            return " DELETE :: Operateur controller";
        }

}
