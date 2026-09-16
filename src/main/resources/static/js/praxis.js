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
        // Evita duplo envio (dois cliques = duas acoes) e diz que esta em curso.
        var botao = form.querySelector('button[type="submit"]');
        if (botao) {
            var rotulo = botao.textContent;
            botao.setAttribute('aria-busy', 'true');
            botao.dataset.rotulo = rotulo;
            botao.textContent = 'Enviando...';
            botao.disabled = true;
            setTimeout(function () { restaurar(botao); }, 8000);
        }
    });

    function restaurar(botao) {
        if (!botao || !botao.dataset.rotulo) { return; }
        botao.textContent = botao.dataset.rotulo;
        botao.removeAttribute('aria-busy');
        botao.disabled = false;
        delete botao.dataset.rotulo;
    }

    // Voltar pelo historico traz a pagina do cache com o botao ainda travado.
    window.addEventListener('pageshow', function () {
        document.querySelectorAll('button[aria-busy="true"]').forEach(restaurar);
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

/* Tela de documentos: campos do modelo escolhido viram inputs; campos de peca
   compilada aparecem so para o tipo a que pertencem. */
(function () {
    'use strict';
    var select = document.getElementById('codigo-modelo');
    var tipo = document.getElementById('tipo-peca');
    var caixa = document.getElementById('campos-modelo');
    if (!select || !caixa) { return; }
    var grade = caixa.querySelector('.grade-campos');
    var vazio = caixa.querySelector('.vazio-modelo');
    var compilados = document.querySelectorAll('.so-compilado');

    function atualizar() {
        var opcao = select.options[select.selectedIndex];
        var comModelo = !!select.value;
        var tipoAtual = tipo ? tipo.value : '';

        compilados.forEach(function (el) {
            var tipos = (el.getAttribute('data-tipos') || '').split(',');
            el.hidden = comModelo || tipos.indexOf(tipoAtual) === -1;
        });
        if (tipo) {
            // Nao desabilita (campo desabilitado nao e enviado); so avisa que o tipo vem do modelo.
            tipo.classList.toggle('ignorado', comModelo);
            tipo.title = comModelo ? 'Ignorado: o tipo da peca vem do modelo (' + (opcao.getAttribute('data-tipo') || '') + ')' : '';
        }

        caixa.hidden = !comModelo;
        grade.innerHTML = '';
        if (!comModelo) { return; }
        var campos = (opcao.getAttribute('data-campos') || '').split(',').filter(Boolean);
        vazio.hidden = campos.length > 0;
        campos.forEach(function (nome) {
            var label = document.createElement('label');
            var codigo = document.createElement('code');
            codigo.textContent = '{{' + nome + '}}';
            var input = document.createElement('input');
            input.type = 'text';
            input.name = 'campo_' + nome;
            input.placeholder = 'valor de ' + nome;
            label.appendChild(codigo);
            label.appendChild(input);
            grade.appendChild(label);
        });
    }
    select.addEventListener('change', atualizar);
    if (tipo) { tipo.addEventListener('change', atualizar); }
    atualizar();
})();

/* Tela de modelos: mostra, enquanto se digita, quais {{campos}} serao pedidos
   ao gerar a peca (os reservados vem dos autos e aparecem riscados). */
(function () {
    'use strict';
    var form = document.getElementById('form-modelo');
    var saida = document.getElementById('campos-detectados');
    if (!form || !saida) { return; }
    var reservados = (form.getAttribute('data-reservados') || '').split(',').filter(Boolean);
    var areas = form.querySelectorAll('textarea[data-marcadores]');

    function detectar() {
        var vistos = {};
        var ordem = [];
        areas.forEach(function (a) {
            var re = /\{\{\s*([A-Za-z_][\w]*)\s*\}\}/g, m;
            while ((m = re.exec(a.value)) !== null) {
                if (!vistos[m[1]]) { vistos[m[1]] = true; ordem.push(m[1]); }
            }
        });
        saida.innerHTML = '';
        saida.appendChild(document.createTextNode('Campos que serao pedidos ao gerar a peca: '));
        if (ordem.length === 0) {
            var b = document.createElement('strong');
            b.textContent = 'nenhum ainda';
            saida.appendChild(b);
            return;
        }
        var pedidos = 0;
        ordem.forEach(function (n) {
            var reservado = reservados.indexOf(n) !== -1;
            if (!reservado) { pedidos++; }
            var chip = document.createElement('span');
            chip.className = 'chip' + (reservado ? ' reservado' : '');
            chip.title = reservado ? 'vem dos autos automaticamente' : 'informado ao gerar a peca';
            chip.textContent = n;
            saida.appendChild(chip);
        });
        if (pedidos === 0) {
            var s = document.createElement('strong');
            s.textContent = ' (todos vem dos autos)';
            saida.appendChild(s);
        }
    }
    areas.forEach(function (a) { a.addEventListener('input', detectar); });
    detectar();
})();
