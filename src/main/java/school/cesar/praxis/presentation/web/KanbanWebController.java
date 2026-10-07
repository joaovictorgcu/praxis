package school.cesar.praxis.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class KanbanWebController {

    @GetMapping("/painel/kanban")
    public String kanban(@RequestParam(required = false) String processo, Model model) {
        model.addAttribute("processoSelecionado", processo == null ? "" : processo);
        return "kanban";
    }
}
