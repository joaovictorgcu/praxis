package school.cesar.praxis.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/painel/advogados")
public class AdvogadoWebController {

    @GetMapping
    public String advogados() {
        return "advogados";
    }
}
