package com.stockanalyzer.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards all non-API, non-static-asset requests to index.html so the
 * React single-page app handles its own routing (tab state, direct URL access).
 *
 * Pattern explanation:
 *   /{path:[^\\.]*}  — matches any single path segment with no dot (excludes .js, .css, .png etc.)
 *   /**              — catches deeper paths like /some/nested/route
 *
 * Spring's static resource handler intercepts actual files (.js, .css, etc.)
 * before this controller, so they are served correctly.
 */
@Controller
public class SpaController {

    @GetMapping(value = {"/{path:[^\\.]*}", "/**/{path:[^\\.]*}"})
    public String forward(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Let /api/** routes be handled by RestControllers — this should never
        // match them, but guard explicitly for safety.
        if (path.startsWith("/api/")) {
            return null;
        }
        return "forward:/index.html";
    }
}
