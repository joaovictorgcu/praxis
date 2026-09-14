/* praxis - comportamento minimo das telas. Sem framework, sem inline (CSP). */
(function () {
    'use strict';

    // Confirmacao antes de acao irreversivel: <form data-confirmar="Remover X?">
    document.addEventListener('submit', function (evento) {
        var form = evento.target;
        var pergunta = form.getAttribute('data-confirmar');
        if (pergunta && !window.confirm(pergunta)) {
            evento.preventDefault();
            return;
        }
        // Evita duplo envio (dois cliques = duas acoes).
        var botao = form.querySelector('button[type="submit"]');
        if (botao) {
            botao.disabled = true;
            setTimeout(function () { botao.disabled = false; }, 4000);
        }
    });

    // Mensagem de sucesso some sozinha; a de erro fica.
    var sucesso = document.querySelector('.mensagem.sucesso');
    if (sucesso) {
        setTimeout(function () { sucesso.style.transition = 'opacity .6s'; sucesso.style.opacity = '0'; }, 6000);
    }

    // Foco no primeiro campo com erro de validacao nativa.
    var invalido = document.querySelector(':invalid:not(form)');
    if (invalido && document.activeElement === document.body) {
        try { invalido.focus({ preventScroll: true }); } catch (e) { /* ignora */ }
    }
})();
