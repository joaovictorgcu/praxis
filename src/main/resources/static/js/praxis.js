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

(function () {
    'use strict';
    const raiz = document.querySelector('[data-corpo-juridico]');
    if (!raiz) {
        return;
    }
    const chefe = raiz.dataset.chefe === 'true';
    const modal = document.getElementById('juridico-modal');
    const conteudo = document.getElementById('modal-conteudo');
    const lista = document.getElementById('lista-juridico');
    const busca = document.getElementById('juridico-busca');
    const feedback = document.getElementById('juridico-feedback');
    let advogados = [];
    let equipes = [];
    let aba = 'advogados';
    let carregado = false;
    const normalizar = (texto) =>
        String(texto || '')
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLowerCase();
    function criarElemento(tag, texto, classe) {
        const elemento = document.createElement(tag);
        if (texto !== undefined) {
            elemento.textContent = texto;
        }
        if (classe) {
            elemento.className = classe;
        }
        return elemento;
    }
    function botao(texto, acao, classe) {
        const elemento = criarElemento('button', texto, classe);
        elemento.type = 'button';
        elemento.addEventListener('click', acao);
        return elemento;
    }
    async function requisitarApi(url, metodo, dados) {
        const resposta = await fetch(url, {
            method: metodo || 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-Token': document.querySelector('meta[name="csrf-token"]').content
            },
            body: dados === undefined ? undefined : JSON.stringify(dados)
        });
        if (!resposta.ok) {
            const erro = await resposta.json().catch(() => ({}));
            throw new Error(erro.erro || 'Não foi possível concluir a operação. Tente novamente.');
        }
        return resposta.status === 204 ? null : resposta.json();
    }
    async function carregar() {
        try {
            feedback.textContent = 'Carregando corpo jurídico…';
            [advogados, equipes] = await Promise.all([
                requisitarApi('/api/advogados'),
                requisitarApi('/api/equipes')
            ]);
            advogados.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
            equipes.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
            carregado = true;
            document.getElementById('total-advogados').textContent = advogados.length;
            document.getElementById('total-disponiveis').textContent = advogados.filter(
                (a) => a.disponivel && a.status === 'ATIVO'
            ).length;
            document.getElementById('total-equipes').textContent = equipes.length;
            const sugestoes = document.getElementById('sugestoes-equipes');
            sugestoes.replaceChildren();
            equipes.forEach((e) => {
                const opcao = criarElemento('option');
                opcao.value = e.nome;
                sugestoes.append(opcao);
            });
            feedback.textContent = '';
            renderizar();
        } catch (erro) {
            feedback.replaceChildren(
                criarElemento('span', erro.message),
                botao('Tentar novamente', carregar, 'secundario')
            );
        }
    }
    function abrir(titulo) {
        conteudo.replaceChildren();
        document.getElementById('modal-titulo').textContent = titulo;
        if (!modal.open) {
            modal.showModal();
        }
    }
    function ficha(campos) {
        const dl = criarElemento('dl', undefined, 'juridico-ficha');
        campos.forEach(([rotulo, valor]) => {
            const div = criarElemento('div');
            div.append(criarElemento('dt', rotulo), criarElemento('dd', valor || 'Não informado'));
            dl.append(div);
        });
        return dl;
    }
    function previewAdvogado(a) {
        abrir(a.nome);
        conteudo.append(
            ficha([
                ['Nome completo', a.nome],
                ['OAB', a.oab],
                ['E-mail', a.email],
                ['Telefone', a.telefone],
                ['Especialidade', a.especialidade],
                ['Status', a.status === 'ATIVO' ? 'Ativo' : 'Desativado'],
                ['Disponibilidade', a.disponivel ? 'Disponível' : 'Indisponível'],
                ['Admissão', a.dataAdmissao ? a.dataAdmissao.split('-').reverse().join('/') : ''],
                [
                    'Equipes',
                    equipes
                        .filter((e) => e.membrosIds.includes(a.id))
                        .map((e) => e.nome)
                        .join(', ') || 'Sem equipe'
                ]
            ])
        );
        if (chefe) {
            conteudo.append(botao('Editar advogado', () => editarAdvogado(a)));
        }
    }
    function previewEquipe(e) {
        abrir(e.nome);
        conteudo.append(criarElemento('p', e.membrosIds.length + ' integrante(s)', 'apoio'));
        const membros = advogados.filter((a) => e.membrosIds.includes(a.id));
        membros.forEach((a) => {
            const div = criarElemento('div', undefined, 'juridico-membro');
            const dados = criarElemento('div');
            dados.append(
                botao(a.nome, () => previewAdvogado(a), 'juridico-link'),
                criarElemento(
                    'small',
                    a.oab +
                        ' · ' +
                        (a.especialidade || 'Sem especialidade') +
                        ' · ' +
                        (a.status === 'ATIVO' ? 'Ativo' : 'Desativado')
                )
            );
            div.append(dados);
            conteudo.append(div);
        });
        if (!membros.length) {
            conteudo.append(criarElemento('p', 'Esta equipe ainda não tem advogados.', 'vazio'));
        }
        if (chefe) {
            conteudo.append(botao('Editar equipe', () => editarEquipe(e), 'secundario'));
        }
    }
    function menu(item) {
        const details = criarElemento('details', undefined, 'juridico-acoes');
        const summary = criarElemento('summary', '⋯');
        summary.setAttribute('aria-label', 'Opções de ' + item.nome);
        const caixa = criarElemento('div', undefined, 'juridico-menu');
        caixa.append(
            botao('Editar', () => {
                details.open = false;
                aba === 'advogados' ? editarAdvogado(item) : editarEquipe(item);
            })
        );
        if (aba === 'equipes') {
            caixa.append(botao('Excluir equipe', () => confirmarExclusao(item), 'excluir'));
        } else {
            caixa.append(
                botao(item.status === 'ATIVO' ? 'Desativar' : 'Ativar', async () => {
                    details.open = false;
                    try {
                        await requisitarApi(
                            '/api/advogados/' +
                                item.id +
                                (item.status === 'ATIVO' ? '/desativar' : '/ativar'),
                            'POST'
                        );
                        await carregar();
                    } catch (erro) {
                        feedback.textContent = erro.message;
                    }
                })
            );
        }
        details.append(summary, caixa);
        return details;
    }
    function renderizar() {
        if (!carregado) {
            return;
        }
        lista.replaceChildren();
        const termo = normalizar(busca.value);
        const itens = (aba === 'advogados' ? advogados : equipes).filter((a) =>
            normalizar([a.nome, a.oab, a.email, a.especialidade].join(' ')).includes(termo)
        );
        if (!itens.length) {
            lista.append(
                criarElemento(
                    'p',
                    termo
                        ? 'Nenhum resultado encontrado. Experimente outro termo.'
                        : aba === 'equipes'
                          ? 'Organize seu escritório: crie sua primeira equipe.'
                          : 'Nenhum advogado cadastrado.',
                    'vazio'
                )
            );
            return;
        }
        const tabela = criarElemento('table');
        const head = criarElemento('thead');
        const tr = criarElemento('tr');
        (aba === 'advogados'
            ? ['Advogado', 'OAB', 'Especialidade', 'Status', 'Disponibilidade']
            : ['Equipe', 'Integrantes', 'Advogados']
        ).forEach((t) => tr.append(criarElemento('th', t)));
        if (chefe) {
            tr.append(criarElemento('th', 'Opções'));
        }
        head.append(tr);
        const body = criarElemento('tbody');
        itens.forEach((item) => {
            const linha = criarElemento('tr', undefined, 'juridico-linha');
            const exibirTarefa = () => (aba === 'advogados' ? previewAdvogado(item) : previewEquipe(item));
            const nome = criarElemento('td');
            nome.append(botao(item.nome, exibirTarefa, 'juridico-link'));
            linha.append(nome);
            const valores =
                aba === 'advogados'
                    ? [
                          item.oab,
                          item.especialidade || '—',
                          item.status === 'ATIVO' ? 'Ativo' : 'Desativado',
                          item.disponivel ? 'Disponível' : 'Indisponível'
                      ]
                    : [
                          String(item.membrosIds.length),
                          advogados
                              .filter((a) => item.membrosIds.includes(a.id))
                              .map((a) => a.nome)
                              .join(', ') || 'Sem integrantes'
                      ];
            valores.forEach((v, i) => {
                const td = criarElemento('td');
                td.append(
                    aba === 'advogados' && i === 2
                        ? criarElemento('span', v, 'etiqueta ' + (item.status === 'ATIVO' ? 'verde' : 'cinza'))
                        : criarElemento('span', v)
                );
                linha.append(td);
            });
            if (chefe) {
                const td = criarElemento('td');
                td.append(menu(item));
                linha.append(td);
            }
            linha.addEventListener('click', (evento) => {
                if (!evento.target.closest('button, details')) {
                    exibirTarefa();
                }
            });
            body.append(linha);
        });
        tabela.append(head, body);
        const wrapper = criarElemento('div', undefined, 'tabela');
        wrapper.append(tabela);
        lista.append(wrapper);
    }
    function salvarForm(form, executar) {
        form.addEventListener('submit', async (evento) => {
            evento.preventDefault();
            evento.stopPropagation();
            const submit = form.querySelector('[type="submit"]');
            const erro = form.querySelector('[data-form-erro]');
            erro.hidden = true;
            submit.disabled = true;
            submit.textContent = 'Salvando…';
            try {
                await executar();
                modal.close();
                await carregar();
                feedback.textContent = 'Alterações salvas com sucesso.';
            } catch (falha) {
                erro.textContent = falha.message;
                erro.hidden = false;
            } finally {
                submit.disabled = false;
                submit.textContent = 'Salvar';
            }
        });
    }
    function editarAdvogado(a) {
        abrir(a ? 'Editar advogado' : 'Cadastrar advogado');
        conteudo.append(document.getElementById('form-advogado').content.cloneNode(true));
        const form = conteudo.querySelector('form');
        if (a) {
            ['nome', 'email', 'oab', 'telefone', 'especialidade'].forEach((n) => {
                form.elements[n].value = a[n] || '';
            });
            form.elements.disponivel.checked = a.disponivel;
            form.elements.oab.disabled = true;
            form.querySelector('[data-oab-aviso]').hidden = false;
        }
        salvarForm(form, () => {
            const dados = Object.fromEntries(new FormData(form));
            dados.disponivel = form.elements.disponivel.checked;
            return requisitarApi('/api/advogados' + (a ? '/' + a.id : ''), a ? 'PUT' : 'POST', dados);
        });
    }
    function editarEquipe(e) {
        abrir(e ? 'Editar equipe' : 'Criar equipe');
        conteudo.append(document.getElementById('form-equipe').content.cloneNode(true));
        const form = conteudo.querySelector('form');
        const selecionados = new Set(e ? e.membrosIds : []);
        form.elements.nome.value = e ? e.nome : '';
        const pesquisa = form.querySelector('[data-busca-membros]');
        const caixa = form.querySelector('[data-membros]');
        const contagem = form.querySelector('[data-contagem-membros]');
        function mostrarMembros() {
            caixa.replaceChildren();
            contagem.textContent = selecionados.size + ' advogado(s) selecionado(s)';
            const filtrados = advogados.filter((a) =>
                normalizar([a.nome, a.oab, a.especialidade].join(' ')).includes(normalizar(pesquisa.value))
            );
            filtrados.forEach((a) => {
                const label = criarElemento('label', undefined, 'juridico-membro');
                const check = criarElemento('input');
                check.type = 'checkbox';
                check.checked = selecionados.has(a.id);
                check.addEventListener('change', () => {
                    check.checked ? selecionados.add(a.id) : selecionados.delete(a.id);
                    contagem.textContent = selecionados.size + ' advogado(s) selecionado(s)';
                });
                const dados = criarElemento('span', a.nome);
                dados.append(
                    criarElemento(
                        'small',
                        a.oab +
                            ' · ' +
                            (a.especialidade || 'Sem especialidade') +
                            (a.status === 'ATIVO' ? '' : ' · Desativado')
                    )
                );
                label.append(check, dados);
                caixa.append(label);
            });
            if (!filtrados.length) {
                caixa.append(criarElemento('p', 'Nenhum advogado encontrado.', 'vazio'));
            }
        }
        pesquisa.addEventListener('input', mostrarMembros);
        mostrarMembros();
        salvarForm(form, () =>
            requisitarApi('/api/equipes' + (e ? '/' + e.id : ''), e ? 'PUT' : 'POST', {
                nome: form.elements.nome.value,
                membrosIds: Array.from(selecionados)
            })
        );
    }
    function confirmarExclusao(e) {
        abrir('Excluir equipe');
        conteudo.append(
            criarElemento(
                'p',
                'Excluir a equipe “' +
                    e.nome +
                    '”? Todos os vínculos dos ' +
                    e.membrosIds.length +
                    ' integrante(s) serão removidos. Os cadastros dos advogados serão preservados.'
            )
        );
        const erro = criarElemento('p', '', 'mensagem erro');
        erro.hidden = true;
        erro.setAttribute('role', 'alert');
        conteudo.append(erro);
        const rodape = criarElemento('div', undefined, 'juridico-rodape');
        rodape.append(botao('Cancelar', () => modal.close(), 'secundario'));
        const excluir = botao(
            'Excluir equipe',
            async () => {
                excluir.disabled = true;
                try {
                    await requisitarApi('/api/equipes/' + e.id, 'DELETE');
                    modal.close();
                    await carregar();
                    feedback.textContent = 'Equipe excluída e vínculos removidos.';
                } catch (falha) {
                    erro.textContent = falha.message;
                    erro.hidden = false;
                } finally {
                    excluir.disabled = false;
                }
            },
            'perigo'
        );
        rodape.append(excluir);
        conteudo.append(rodape);
    }
    function trocarAba(valor) {
        aba = valor;
        busca.value = '';
        raiz.querySelectorAll('[data-aba]').forEach((b) => {
            b.setAttribute('aria-selected', String(b.dataset.aba === aba));
            b.tabIndex = b.dataset.aba === aba ? 0 : -1;
        });
        lista.setAttribute('aria-labelledby', 'aba-' + aba);
        document.getElementById('titulo-lista').textContent =
            aba === 'advogados' ? 'Advogados do escritório' : 'Equipes do escritório';
        document.getElementById('apoio-lista').textContent =
            aba === 'advogados'
                ? 'Selecione um profissional para ver sua ficha completa.'
                : 'Selecione uma equipe para conhecer seus integrantes.';
        busca.placeholder =
            aba === 'advogados' ? 'Nome, OAB, e-mail ou especialidade' : 'Pesquisar equipes pelo nome';
        if (aba === 'equipes') {
            busca.setAttribute('list', 'sugestoes-equipes');
        } else {
            busca.removeAttribute('list');
        }
        const novo = document.getElementById('juridico-novo');
        if (novo) {
            novo.textContent = aba === 'advogados' ? 'Cadastrar advogado' : 'Criar equipe';
        }
        renderizar();
    }
    raiz.querySelectorAll('[data-aba]').forEach((b) => {
        b.addEventListener('click', () => trocarAba(b.dataset.aba));
        b.addEventListener('keydown', (evento) => {
            if (['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(evento.key)) {
                evento.preventDefault();
                trocarAba(
                    evento.key === 'Home'
                        ? 'advogados'
                        : evento.key === 'End'
                          ? 'equipes'
                          : aba === 'advogados'
                            ? 'equipes'
                            : 'advogados'
                );
                document.getElementById('aba-' + aba).focus();
            }
        });
    });
    const novo = document.getElementById('juridico-novo');
    if (novo) {
        novo.addEventListener('click', () => {
            if (carregado) {
                aba === 'advogados' ? editarAdvogado() : editarEquipe();
            }
        });
    }
    busca.addEventListener('input', renderizar);
    modal.addEventListener('click', (evento) => {
        if (evento.target.closest('[data-fechar]')) {
            modal.close();
            return;
        }
        if (evento.target === modal) {
            const limites = modal.getBoundingClientRect();
            if (
                evento.clientX < limites.left ||
                evento.clientX > limites.right ||
                evento.clientY < limites.top ||
                evento.clientY > limites.bottom
            ) {
                modal.close();
            }
        }
    });
    document.addEventListener('click', (evento) => {
        document.querySelectorAll('.juridico-acoes[open]').forEach((d) => {
            if (!d.contains(evento.target)) {
                d.open = false;
            }
        });
    });
    trocarAba('advogados');
    carregar();
})();

(function () {
    'use strict';
    const raiz = document.querySelector('[data-jurisdicao]');
    if (!raiz) {
        return;
    }
    const chefe = raiz.dataset.chefe === 'true';
    const lista = document.getElementById('foro-lista');
    const busca = document.getElementById('foro-busca');
    const estado = document.getElementById('foro-estado');
    const filtroComarca = document.getElementById('foro-comarca');
    const competencia = document.getElementById('foro-competencia');
    const feedback = document.getElementById('foro-feedback');
    const modal = document.getElementById('foro-modal');
    const conteudo = document.getElementById('foro-modal-conteudo');
    const ufs = 'AC AL AP AM BA CE DF ES GO MA MT MS MG PA PB PR PE PI RJ RN RS RO RR SC SP SE TO'.split(' ');
    let comarcas = [];
    let varas = [];
    let aba = 'comarcas';
    let carregado = false;
    const normalizar = (valor) =>
        String(valor || '')
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLowerCase();
    const data = (valor) => (valor ? valor.split('-').reverse().join('/') : 'Não informado');
    function criarElemento(tag, texto, classe) {
        const elemento = document.createElement(tag);
        if (texto !== undefined) {
            elemento.textContent = texto;
        }
        if (classe) {
            elemento.className = classe;
        }
        return elemento;
    }
    function botao(texto, acao, classe) {
        const elemento = criarElemento('button', texto, classe);
        elemento.type = 'button';
        elemento.addEventListener('click', acao);
        return elemento;
    }
    function icone() {
        const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('viewBox', '0 0 24 24');
        svg.setAttribute('aria-hidden', 'true');
        const use = document.createElementNS('http://www.w3.org/2000/svg', 'use');
        use.setAttribute('href', '#i-comarcas');
        svg.append(use);
        return svg;
    }
    function opcao(select, valor, texto) {
        const option = criarElemento('option', texto);
        option.value = valor;
        select.append(option);
    }
    function preencherOpcoes(select, itens) {
        const anterior = select.value;
        while (select.options.length > 1) {
            select.remove(1);
        }
        itens.forEach(([valor, texto]) => opcao(select, valor, texto));
        if (itens.some(([valor]) => String(valor) === anterior)) {
            select.value = anterior;
        }
    }
    async function requisitarApi(url, metodo, dados) {
        const resposta = await fetch(url, {
            method: metodo || 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-Token': document.querySelector('meta[name="csrf-token"]').content
            },
            body: dados === undefined ? undefined : JSON.stringify(dados)
        });
        if (!resposta.ok) {
            const erro = await resposta.json().catch(() => ({}));
            throw new Error(erro.erro || 'Não foi possível concluir a operação. Tente novamente.');
        }
        return resposta.json();
    }
    async function carregar() {
        feedback.className = '';
        feedback.textContent = 'Carregando comarcas e varas…';
        try {
            [comarcas, varas] = await Promise.all([
                requisitarApi('/api/comarcas'),
                requisitarApi('/api/varas')
            ]);
            const ordenar = (a, b) => a.nome.localeCompare(b.nome, 'pt-BR');
            comarcas.sort(ordenar);
            varas.sort(ordenar);
            carregado = true;
            document.getElementById('foro-total-comarcas').textContent = comarcas.length;
            document.getElementById('foro-total-varas').textContent = varas.length;
            document.getElementById('foro-total-estados').textContent = new Set(
                comarcas.map((c) => c.uf)
            ).size;
            document.getElementById('foro-contagem-comarcas').textContent = comarcas.length;
            document.getElementById('foro-contagem-varas').textContent = varas.length;
            preencherOpcoes(
                estado,
                [...new Set(comarcas.map((c) => c.uf))].sort().map((uf) => [uf, uf])
            );
            preencherOpcoes(
                filtroComarca,
                comarcas.map((c) => [c.id, c.nome + ' · ' + c.uf])
            );
            preencherOpcoes(
                competencia,
                [...new Set(varas.map((v) => v.competencia))]
                    .sort((a, b) => a.localeCompare(b, 'pt-BR'))
                    .map((c) => [c, c])
            );
            feedback.textContent = '';
            renderizar();
            return true;
        } catch (erro) {
            feedback.replaceChildren(
                criarElemento('p', erro.message, 'mensagem erro'),
                botao('Tentar novamente', carregar, 'secundario')
            );
            return false;
        }
    }
    const comarcaDa = (vara) => comarcas.find((c) => c.id === vara.comarcaId);
    function renderizar() {
        if (!carregado) {
            return;
        }
        lista.replaceChildren();
        const termo = normalizar(busca.value);
        const filtrando = Boolean(
            termo || estado.value || (aba === 'varas' && (filtroComarca.value || competencia.value))
        );
        const itens = (aba === 'comarcas' ? comarcas : varas).filter((item) => {
            const c = aba === 'comarcas' ? item : comarcaDa(item);
            return (
                normalizar(
                    [item.nome, item.competencia, c?.nome, c?.municipio, c?.tribunal, c?.uf].join(' ')
                ).includes(termo) &&
                (!estado.value || c?.uf === estado.value) &&
                (aba === 'comarcas' ||
                    ((!filtroComarca.value || item.comarcaId === Number(filtroComarca.value)) &&
                        (!competencia.value || item.competencia === competencia.value)))
            );
        });
        document.getElementById('foro-limpar').hidden = !filtrando;
        document.getElementById('foro-resultados').textContent =
            itens.length +
            ' de ' +
            (aba === 'comarcas' ? comarcas.length : varas.length) +
            (aba === 'comarcas' ? ' comarcas' : ' varas');
        if (!itens.length) {
            const vazio = criarElemento('div', undefined, 'foro-vazio');
            vazio.append(
                icone(),
                criarElemento(
                    'h3',
                    filtrando
                        ? 'Nenhum resultado por aqui'
                        : aba === 'comarcas'
                          ? 'Seu mapa começa com uma comarca'
                          : 'Cada vara tem seu lugar'
                ),
                criarElemento(
                    'p',
                    filtrando
                        ? 'Ajuste a pesquisa ou limpe os filtros para ver todos os cadastros.'
                        : aba === 'comarcas'
                          ? 'Cadastre a primeira comarca e reúna suas varas e informações de atendimento.'
                          : 'Cadastre uma vara, vincule à comarca e informe sua competência.'
                )
            );
            if (filtrando) {
                vazio.append(botao('Limpar filtros', limparFiltros, 'secundario'));
            } else if (chefe) {
                vazio.append(
                    botao(aba === 'comarcas' ? 'Cadastrar comarca' : 'Cadastrar vara', () =>
                        aba === 'comarcas' ? editarComarca() : editarVara()
                    )
                );
            }
            lista.append(vazio);
            return;
        }
        itens.forEach((item) => {
            const c = aba === 'comarcas' ? item : comarcaDa(item);
            const card = criarElemento('article', undefined, 'foro-unidade');
            const exibirTarefa = () => (aba === 'comarcas' ? previewComarca(item) : previewVara(item));
            const topo = criarElemento('div', undefined, 'foro-unidade-topo');
            topo.append(
                botao(item.nome, exibirTarefa, 'foro-nome'),
                criarElemento('span', c?.uf || '—', 'etiqueta azul')
            );
            card.append(
                topo,
                criarElemento(
                    'p',
                    aba === 'comarcas'
                        ? item.municipio + ' · ' + item.uf
                        : c?.nome || 'Comarca não encontrada',
                    'foro-local'
                ),
                criarElemento('p', c?.tribunal || '—', 'foro-tribunal')
            );
            const chips = criarElemento('div', undefined, 'foro-chips');
            if (aba === 'comarcas') {
                const vinculadas = varas.filter((v) => v.comarcaId === item.id);
                chips.append(
                    criarElemento(
                        'span',
                        vinculadas.length + (vinculadas.length === 1 ? ' vara' : ' varas'),
                        'etiqueta cinza'
                    )
                );
                [...new Set(vinculadas.map((v) => v.competencia))]
                    .slice(0, 2)
                    .forEach((valor) => chips.append(criarElemento('span', valor, 'etiqueta cinza')));
            } else {
                chips.append(criarElemento('span', item.competencia, 'etiqueta azul'));
            }
            card.append(chips);
            const rodape = criarElemento('div', undefined, 'foro-unidade-rodape');
            rodape.append(botao('Ver detalhes →', exibirTarefa, 'foro-detalhes-link'));
            if (chefe) {
                const editar = botao(
                    'Editar',
                    () => (aba === 'comarcas' ? editarComarca(item) : editarVara(item)),
                    'secundario pequeno'
                );
                editar.setAttribute('aria-label', 'Editar ' + item.nome);
                rodape.append(editar);
            }
            card.append(rodape);
            card.addEventListener('click', (evento) => {
                if (!evento.target.closest('button, a')) {
                    exibirTarefa();
                }
            });
            lista.append(card);
        });
    }
    function abrir(titulo, subtitulo) {
        conteudo.replaceChildren();
        document.getElementById('foro-modal-titulo').textContent = titulo;
        document.getElementById('foro-modal-subtitulo').textContent = subtitulo;
        if (!modal.open) {
            modal.showModal();
        }
        modal.scrollTop = 0;
    }
    function ficha(campos) {
        const dl = criarElemento('dl', undefined, 'foro-ficha');
        campos.forEach(([nome, valor, tipo]) => {
            const grupo = criarElemento('div', undefined, tipo === 'largo' ? 'foro-largo' : '');
            const dd = criarElemento('dd');
            if (valor && ['email', 'telefone'].includes(tipo)) {
                const link = criarElemento('a', valor);
                link.href =
                    tipo === 'email'
                        ? 'mailto:' + encodeURIComponent(valor)
                        : 'tel:' + valor.replace(/[^\d+]/g, '');
                dd.append(link);
            } else {
                dd.textContent = valor || 'Não informado';
            }
            grupo.append(criarElemento('dt', nome), dd);
            dl.append(grupo);
        });
        return dl;
    }
    function contatos(item) {
        return [
            ['Endereço', item.endereco, 'largo'],
            ['Telefone', item.telefone, 'telefone'],
            ['E-mail', item.email, 'email'],
            ['Atendimento', item.horarioAtendimento],
            ['Cadastrado em', data(item.dataCadastro)],
            ['Observações', item.observacoes, 'largo']
        ];
    }
    function previewComarca(c) {
        abrir(c.nome, 'FICHA DA COMARCA');
        conteudo.append(
            ficha([
                ['Nome da comarca', c.nome],
                ['Município sede', c.municipio],
                ['Estado', c.uf],
                ['Tribunal', c.tribunal],
                ...contatos(c)
            ])
        );
        const vinculadas = varas.filter((v) => v.comarcaId === c.id);
        const secao = criarElemento('section', undefined, 'foro-varas-preview');
        secao.append(criarElemento('h3', 'Varas desta comarca · ' + vinculadas.length));
        if (!vinculadas.length) {
            secao.append(criarElemento('p', 'Nenhuma vara vinculada a esta comarca ainda.', 'apoio'));
        }
        vinculadas.forEach((v) => {
            const linha = criarElemento('div', undefined, 'foro-vara-preview');
            const texto = criarElemento('div');
            texto.append(
                botao(v.nome, () => previewVara(v), 'foro-nome'),
                criarElemento('small', v.competencia)
            );
            linha.append(
                texto,
                botao('Ver ficha', () => previewVara(v), 'secundario pequeno')
            );
            secao.append(linha);
        });
        conteudo.append(secao);
        const rodape = criarElemento('div', undefined, 'foro-form-rodape');
        if (chefe) {
            rodape.append(
                botao('+ Adicionar vara', () => editarVara(undefined, c.id), 'secundario'),
                botao('Editar comarca', () => editarComarca(c))
            );
        } else {
            rodape.append(botao('Fechar', () => modal.close(), 'secundario'));
        }
        conteudo.append(rodape);
    }
    function previewVara(v) {
        const c = comarcaDa(v);
        abrir(v.nome, 'FICHA DA VARA');
        conteudo.append(
            ficha([
                ['Nome da vara', v.nome],
                ['Competência', v.competencia],
                ['Comarca', c?.nome],
                ['Município / UF', c ? c.municipio + ' / ' + c.uf : ''],
                ['Tribunal', c?.tribunal],
                ...contatos(v)
            ])
        );
        if (c) {
            const linha = criarElemento('div', undefined, 'foro-vara-preview');
            const texto = criarElemento('div');
            texto.append(
                criarElemento('strong', 'Informações da comarca'),
                criarElemento('small', c.endereco || 'Consulte a comarca para ver seus contatos e outras varas.')
            );
            linha.append(
                texto,
                botao('Ver comarca', () => previewComarca(c), 'secundario pequeno')
            );
            conteudo.append(linha);
        }
        if (chefe) {
            const rodape = criarElemento('div', undefined, 'foro-form-rodape');
            rodape.append(botao('Editar vara', () => editarVara(v)));
            conteudo.append(rodape);
        }
    }
    function prepararForm(tipo, item) {
        conteudo.append(document.getElementById('foro-form-' + tipo).content.cloneNode(true));
        const form = conteudo.querySelector('form');
        if (tipo === 'comarca') {
            ufs.forEach((uf) => opcao(form.elements.uf, uf, uf));
            [...new Set(comarcas.map((c) => c.tribunal))].forEach((t) =>
                opcao(form.querySelector('datalist'), t, t)
            );
        } else {
            comarcas.forEach((c) =>
                opcao(form.elements.comarcaId, c.id, c.nome + ' · ' + c.municipio + '/' + c.uf)
            );
        }
        if (item) {
            Array.from(form.elements).forEach((campo) => {
                if (campo.name && item[campo.name] !== undefined) {
                    campo.value = item[campo.name] ?? '';
                }
            });
        }
        form.addEventListener('submit', async (evento) => {
            evento.preventDefault();
            evento.stopPropagation();
            const submit = form.querySelector('[type="submit"]');
            const rotulo = submit.textContent;
            const erro = form.querySelector('[data-foro-erro]');
            erro.hidden = true;
            submit.disabled = true;
            submit.textContent = 'Salvando…';
            submit.setAttribute('aria-busy', 'true');
            const dados = Object.fromEntries(new FormData(form));
            if (tipo === 'vara') {
                dados.comarcaId = Number(dados.comarcaId);
            }
            try {
                await requisitarApi(
                    '/api/' + (tipo === 'comarca' ? 'comarcas' : 'varas') + (item ? '/' + item.id : ''),
                    item ? 'PUT' : 'POST',
                    dados
                );
                modal.close();
                if (await carregar()) {
                    feedback.className = 'foro-feedback-sucesso';
                    feedback.textContent =
                        (tipo === 'comarca' ? 'Comarca' : 'Vara') +
                        (item ? ' atualizada' : ' cadastrada') +
                        ' com sucesso.';
                }
            } catch (falha) {
                erro.textContent = falha.message;
                erro.hidden = false;
                erro.scrollIntoView({ block: 'nearest' });
            } finally {
                submit.disabled = false;
                submit.textContent = rotulo;
                submit.removeAttribute('aria-busy');
            }
        });
        return form;
    }
    function editarComarca(c) {
        abrir(c ? 'Editar comarca' : 'Nova comarca', 'CADASTRO JUDICIÁRIO');
        prepararForm('comarca', c);
        conteudo.querySelector('[name="nome"]').focus();
    }
    function editarVara(v, comarcaId) {
        if (!comarcas.length) {
            abrir('Comece pela comarca', 'VINCULE CADA VARA AO SEU LUGAR');
            conteudo.append(
                criarElemento('p', 'Para cadastrar uma vara, primeiro cadastre a comarca à qual ela pertence.')
            );
            conteudo.append(botao('Cadastrar comarca', () => editarComarca()));
            return;
        }
        abrir(v ? 'Editar vara' : 'Nova vara', 'CADASTRO JUDICIÁRIO');
        const form = prepararForm('vara', v);
        if (!v && comarcaId) {
            form.elements.comarcaId.value = comarcaId;
        } else if (!v && filtroComarca.value) {
            form.elements.comarcaId.value = filtroComarca.value;
        }
        form.elements[v || comarcaId ? 'nome' : 'comarcaId'].focus();
    }
    function limparFiltros() {
        busca.value = '';
        estado.value = '';
        filtroComarca.value = '';
        competencia.value = '';
        renderizar();
    }
    function trocarAba(valor) {
        aba = valor;
        limparFiltros();
        raiz.querySelectorAll('[data-foro-aba]').forEach((b) => {
            b.setAttribute('aria-selected', String(b.dataset.foroAba === aba));
            b.tabIndex = b.dataset.foroAba === aba ? 0 : -1;
        });
        lista.setAttribute('aria-labelledby', 'foro-aba-' + aba);
        document.getElementById('foro-label-comarca').hidden = aba !== 'varas';
        document.getElementById('foro-label-competencia').hidden = aba !== 'varas';
        document.getElementById('foro-titulo-lista').textContent =
            aba === 'comarcas' ? 'Diretório de comarcas' : 'Diretório de varas';
        document.getElementById('foro-descricao-lista').textContent =
            aba === 'comarcas'
                ? 'Uma visão das localidades e das varas que compõem cada comarca.'
                : 'Encontre a unidade certa por comarca e área de competência.';
        busca.placeholder =
            aba === 'comarcas'
                ? 'Nome da comarca, município ou tribunal'
                : 'Nome da vara, comarca ou competência';
        renderizar();
    }
    raiz.querySelectorAll('[data-foro-aba]').forEach((b) => {
        b.addEventListener('click', () => trocarAba(b.dataset.foroAba));
        b.addEventListener('keydown', (evento) => {
            if (['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(evento.key)) {
                evento.preventDefault();
                trocarAba(
                    evento.key === 'Home'
                        ? 'comarcas'
                        : evento.key === 'End'
                          ? 'varas'
                          : aba === 'comarcas'
                            ? 'varas'
                            : 'comarcas'
                );
                document.getElementById('foro-aba-' + aba).focus();
            }
        });
    });
    raiz.querySelectorAll('[data-nova-comarca]').forEach((b) =>
        b.addEventListener('click', () => {
            if (carregado) {
                editarComarca();
            }
        })
    );
    raiz.querySelectorAll('[data-nova-vara]').forEach((b) =>
        b.addEventListener('click', () => {
            if (carregado) {
                editarVara();
            }
        })
    );
    busca.addEventListener('input', renderizar);
    [estado, filtroComarca, competencia].forEach((f) => f.addEventListener('change', renderizar));
    document.getElementById('foro-limpar').addEventListener('click', limparFiltros);
    modal.addEventListener('click', (evento) => {
        if (evento.target.closest('[data-foro-fechar]')) {
            modal.close();
            return;
        }
        if (evento.target === modal) {
            const r = modal.getBoundingClientRect();
            if (
                evento.clientX < r.left ||
                evento.clientX > r.right ||
                evento.clientY < r.top ||
                evento.clientY > r.bottom
            ) {
                modal.close();
            }
        }
    });
    carregar();
})();

(function () {
    'use strict';
    const raiz = document.querySelector('[data-kanban]');
    if (!raiz) {
        return;
    }
    const chefe = raiz.dataset.chefe === 'true';
    const quadroColunas = document.getElementById('kb-colunas');
    const feedback = document.getElementById('kb-feedback');
    const modal = document.getElementById('kb-modal');
    const conteudo = document.getElementById('kb-modal-conteudo');
    const processo = document.getElementById('kb-processo');
    const busca = document.getElementById('kb-busca');
    const responsavel = document.getElementById('kb-responsavel');
    const prioridade = document.getElementById('kb-prioridade');
    const vencidas = document.getElementById('kb-so-vencidas');
    const rotulos = { BAIXA: 'Baixa', NORMAL: 'Normal', ALTA: 'Alta', URGENTE: 'Urgente' };
    const coresPrioridade = { BAIXA: 'cinza', NORMAL: 'azul', ALTA: 'ambar', URGENTE: 'vermelha' };
    let quadro = null;
    let advogados = [];
    let ocupado = false;
    let carregando = false;
    let inicial = true;
    let arrastando = null;
    let ignorarClique = false;
    let previaArrasto = null;
    let toque = null;
    let alvo = null;
    const placeholder = document.createElement('div');
    placeholder.className = 'kb-placeholder';
    const normalizar = (texto) =>
        String(texto || '')
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLowerCase();
    const formatarData = (valor) => (valor ? valor.split('-').reverse().join('/') : 'Sem vencimento');
    const colunaDa = (t) => quadro.colunas.find((c) => c.id === t.colunaId);
    const processoDa = (t) => quadro.processos.find((p) => p.numero === t.numeroProcesso);
    const responsavelDa = (t) => advogados.find((a) => a.id === t.responsavelId);
    const atrasada = (t) => !colunaDa(t)?.conclusiva && t.vencimento && t.vencimento < quadro.hoje;
    function criarElemento(tag, texto, classe) {
        const elemento = document.createElement(tag);
        if (texto !== undefined) {
            elemento.textContent = texto;
        }
        if (classe) {
            elemento.className = classe;
        }
        return elemento;
    }
    function botao(texto, acao, classe) {
        const elemento = criarElemento('button', texto, classe);
        elemento.type = 'button';
        elemento.addEventListener('click', acao);
        return elemento;
    }
    function opcao(select, valor, texto) {
        const option = criarElemento('option', texto);
        option.value = valor;
        select.append(option);
    }
    function erroForm(form, mensagem) {
        const erro = form.querySelector('[data-kb-erro]');
        erro.textContent = mensagem;
        erro.hidden = false;
        erro.scrollIntoView({ block: 'nearest' });
    }
    async function requisitarApi(url, metodo, dados) {
        const resposta = await fetch(url, {
            method: metodo || 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-Token': document.querySelector('meta[name="csrf-token"]').content
            },
            body: dados === undefined ? undefined : JSON.stringify(dados)
        });
        if (!resposta.ok) {
            const erro = await resposta.json().catch(() => ({}));
            throw new Error(
                erro.erro ||
                    (resposta.status === 401
                        ? 'Sua sessão expirou. Faça login novamente.'
                        : 'Não foi possível salvar. Atualize o quadro e tente novamente.')
            );
        }
        return resposta.status === 204 ? null : resposta.json();
    }
    function aplicar(novo) {
        quadro = novo;
        const selecionado = inicial ? raiz.dataset.processo : processo.value;
        processo.replaceChildren();
        opcao(processo, '', 'Todos os processos');
        quadro.processos.forEach((p) => opcao(processo, p.numero, p.numero + ' · ' + p.cliente));
        if (quadro.processos.some((p) => p.numero === selecionado)) {
            processo.value = selecionado;
        } else if (inicial && selecionado) {
            feedback.textContent = 'O processo solicitado não está disponível para esta sessão.';
        }
        inicial = false;
        const escolhido = responsavel.value;
        responsavel.replaceChildren();
        opcao(responsavel, '', 'Todos');
        opcao(responsavel, 'sem', 'Sem responsável');
        advogados.forEach((a) => opcao(responsavel, a.id, a.nome));
        responsavel.value = escolhido;
        renderizar();
    }
    async function carregar() {
        if (ocupado || carregando) {
            return;
        }
        carregando = true;
        document.getElementById('kb-atualizar').disabled = true;
        feedback.className = '';
        feedback.textContent = 'Carregando o quadro…';
        try {
            const [novo, profissionais] = await Promise.all([
                requisitarApi('/api/kanban'),
                requisitarApi('/api/advogados')
            ]);
            advogados = profissionais;
            feedback.textContent = '';
            aplicar(novo);
        } catch (erro) {
            feedback.replaceChildren(
                criarElemento('p', erro.message, 'mensagem erro'),
                botao('Tentar novamente', carregar, 'secundario')
            );
        } finally {
            carregando = false;
            document.getElementById('kb-atualizar').disabled = false;
        }
    }
    async function salvarAlteracao(acao, mensagem) {
        if (ocupado || carregando) {
            throw new Error('Aguarde a operação em andamento.');
        }
        ocupado = true;
        raiz.classList.add('kb-salvando');
        document.getElementById('kb-atualizar').disabled = true;
        try {
            const resultado = await acao();
            aplicar(await requisitarApi('/api/kanban'));
            feedback.className = 'foro-feedback-sucesso';
            feedback.textContent = mensagem;
            return resultado;
        } finally {
            ocupado = false;
            raiz.classList.remove('kb-salvando');
            document.getElementById('kb-atualizar').disabled = false;
        }
    }
    function filtradas() {
        const termo = normalizar(busca.value);
        return quadro.tarefas.filter(
            (t) =>
                (!processo.value || t.numeroProcesso === processo.value) &&
                normalizar(
                    [t.titulo, t.descricao, t.numeroProcesso, processoDa(t)?.cliente].join(' ')
                ).includes(termo) &&
                (!responsavel.value ||
                    (responsavel.value === 'sem'
                        ? !t.responsavelId
                        : t.responsavelId === Number(responsavel.value))) &&
                (!prioridade.value || t.prioridade === prioridade.value) &&
                (!vencidas.checked || atrasada(t))
        );
    }
    function renderizar() {
        if (!quadro) {
            return;
        }
        const tarefas = filtradas();
        const concluidas = tarefas.filter((t) => colunaDa(t)?.conclusiva).length;
        const percentual = tarefas.length ? Math.round((concluidas * 100) / tarefas.length) : 0;
        document.getElementById('kb-total').textContent = tarefas.length;
        document.getElementById('kb-abertas').textContent = tarefas.length - concluidas;
        document.getElementById('kb-concluidas').textContent = concluidas;
        document.getElementById('kb-vencidas').textContent = tarefas.filter(atrasada).length;
        document.getElementById('kb-percentual').textContent = percentual + '%';
        document.getElementById('kb-progresso').value = percentual;
        document.getElementById('kb-resultados').textContent =
            tarefas.length +
            ' tarefa(s) · ' +
            (processo.value ? 'Processo ' + processo.value : 'Todos os processos');
        document.getElementById('kb-limpar').hidden = !(
            processo.value ||
            busca.value ||
            responsavel.value ||
            prioridade.value ||
            vencidas.checked
        );
        const scroll = quadroColunas.scrollLeft;
        quadroColunas.replaceChildren();
        quadro.colunas.forEach((c) => {
            const coluna = criarElemento('section', undefined, 'kb-coluna');
            coluna.dataset.colunaId = c.id;
            coluna.style.setProperty('--kb-cor', c.cor);
            coluna.setAttribute('aria-label', c.nome);
            const itens = tarefas
                .filter((t) => t.colunaId === c.id)
                .sort((a, b) => a.ordem - b.ordem || a.id - b.id);
            const cabecalho = criarElemento('div', undefined, 'kb-coluna-cabecalho');
            const titulo = criarElemento('h2');
            titulo.append(criarElemento('span', '', 'kb-ponto'), criarElemento('span', c.nome));
            cabecalho.append(titulo, criarElemento('span', itens.length, 'etiqueta cinza'));
            coluna.append(cabecalho);
            if (c.conclusiva) {
                coluna.append(criarElemento('span', '✓ Etapa de conclusão', 'kb-final'));
            }
            const cartoes = criarElemento('div', undefined, 'kb-cartoes');
            cartoes.dataset.colunaId = c.id;
            itens.forEach((t) => cartoes.append(cartao(t)));
            if (!itens.length) {
                cartoes.append(
                    criarElemento(
                        'div',
                        'Nenhuma tarefa nesta etapa. Arraste um cartão para cá.',
                        'kb-vazio-coluna'
                    )
                );
            }
            coluna.append(cartoes);
            const adicionar = criarElemento('div', undefined, 'kb-coluna-adicionar');
            adicionar.append(botao('+ Adicionar tarefa', () => editarTarefa(undefined, c.id)));
            coluna.append(adicionar);
            quadroColunas.append(coluna);
        });
        quadroColunas.scrollLeft = scroll;
    }
    function cartao(t) {
        const card = criarElemento('article', undefined, 'kb-cartao');
        card.dataset.tarefaId = t.id;
        card.draggable = true;
        const topo = criarElemento('div', undefined, 'kb-cartao-topo');
        const handle = criarElemento('span', '⠿', 'kb-arrastar');
        handle.title = 'Arrastar tarefa';
        handle.setAttribute('aria-hidden', 'true');
        topo.append(
            criarElemento('span', rotulos[t.prioridade], 'etiqueta ' + coresPrioridade[t.prioridade]),
            handle
        );
        const titulo = botao(
            t.titulo,
            () => {
                if (!ignorarClique && !ocupado) {
                    exibirTarefa(t);
                }
            },
            'kb-titulo'
        );
        card.append(
            topo,
            titulo,
            criarElemento('span', t.numeroProcesso, 'kb-cartao-processo'),
            criarElemento('span', processoDa(t)?.cliente || '', 'kb-cartao-cliente')
        );
        const meta = criarElemento('div', undefined, 'kb-cartao-meta');
        if (t.vencimento) {
            const hoje = !colunaDa(t)?.conclusiva && t.vencimento === quadro.hoje;
            meta.append(
                criarElemento(
                    'span',
                    (atrasada(t) ? 'Vencida · ' : hoje ? 'Hoje · ' : '') + formatarData(t.vencimento),
                    'kb-vencimento' + (atrasada(t) ? ' atrasada' : hoje ? ' hoje' : '')
                )
            );
        }
        if (t.checklist.length) {
            meta.append(
                criarElemento(
                    'span',
                    '☑ ' + t.checklist.filter((i) => i.concluido).length + '/' + t.checklist.length
                )
            );
        }
        card.append(meta);
        const rodape = criarElemento('div', undefined, 'kb-cartao-rodape');
        const pessoa = criarElemento('span', undefined, 'kb-responsavel');
        const a = responsavelDa(t);
        pessoa.append(
            criarElemento(
                'span',
                a
                    ? a.nome
                          .split(/\s+/)
                          .slice(0, 2)
                          .map((n) => n[0])
                          .join('')
                    : '—',
                'kb-avatar'
            ),
            criarElemento('span', a ? a.nome.split(' ')[0] : 'Sem responsável')
        );
        const mover = botao(
            'Mover ↗',
            () => {
                if (!ocupado) {
                    abrirMover(t);
                }
            },
            'secundario'
        );
        mover.setAttribute('aria-label', 'Mover ' + t.titulo);
        rodape.append(pessoa, mover);
        card.append(rodape);
        card.addEventListener('click', (evento) => {
            if (!evento.target.closest('button') && !ignorarClique && !ocupado) {
                exibirTarefa(t);
            }
        });
        card.addEventListener('dragstart', (evento) => {
            if (ocupado || carregando) {
                evento.preventDefault();
                return;
            }
            iniciarArrasto(t, card);
            evento.dataTransfer.effectAllowed = 'move';
            evento.dataTransfer.setData('text/plain', String(t.id));
        });
        card.addEventListener('dragend', finalizarArrasto);
        handle.addEventListener('pointerdown', (evento) => {
            if (evento.pointerType !== 'touch' || ocupado || carregando) {
                return;
            }
            toque = { t, card, x: evento.clientX, y: evento.clientY, handle, id: evento.pointerId };
            handle.setPointerCapture(evento.pointerId);
        });
        return card;
    }
    function abrir(titulo, subtitulo, config) {
        conteudo.replaceChildren();
        modal.classList.toggle('kb-config-modal', Boolean(config));
        document.getElementById('kb-modal-titulo').textContent = titulo;
        document.getElementById('kb-modal-subtitulo').textContent = subtitulo;
        if (!modal.open) {
            modal.showModal();
        }
        modal.scrollTop = 0;
    }
    function ficha(campos) {
        const dl = criarElemento('dl', undefined, 'foro-ficha');
        campos.forEach(([nome, valor]) => {
            const div = criarElemento('div');
            div.append(criarElemento('dt', nome), criarElemento('dd', valor || 'Não informado'));
            dl.append(div);
        });
        return dl;
    }
    function exibirTarefa(t) {
        abrir(t.titulo, 'FICHA DA TAREFA');
        const processoLink = criarElemento('a', t.numeroProcesso + ' · ' + (processoDa(t)?.cliente || ''));
        processoLink.href = '/painel/processos/' + encodeURIComponent(t.numeroProcesso);
        conteudo.append(processoLink);
        conteudo.append(
            ficha([
                ['Etapa', colunaDa(t)?.nome],
                ['Prioridade', rotulos[t.prioridade]],
                ['Responsável', responsavelDa(t)?.nome || 'Sem responsável'],
                ['Vencimento', formatarData(t.vencimento)],
                ['Criada em', formatarData(t.criadaEm)],
                ['Atualizada em', formatarData(t.atualizadaEm)]
            ])
        );
        if (t.descricao) {
            conteudo.append(criarElemento('p', t.descricao, 'kb-descricao'));
        }
        if (t.checklist.length) {
            const box = criarElemento('section', undefined, 'kb-checklist');
            box.append(
                criarElemento(
                    'h3',
                    'Checklist · ' + t.checklist.filter((i) => i.concluido).length + '/' + t.checklist.length
                )
            );
            t.checklist.forEach((item, index) => {
                const label = criarElemento('label', undefined, 'kb-check-item');
                const input = criarElemento('input');
                input.type = 'checkbox';
                input.checked = item.concluido;
                input.addEventListener('change', async () => {
                    box.querySelectorAll('input').forEach((i) => {
                        i.disabled = true;
                    });
                    try {
                        const itens = t.checklist.map((i, n) => ({
                            texto: i.texto,
                            concluido: n === index ? input.checked : i.concluido
                        }));
                        await salvarAlteracao(
                            () =>
                                requisitarApi('/api/kanban/tarefas/' + t.id + '/checklist', 'PUT', {
                                    checklist: itens,
                                    versao: t.versao
                                }),
                            'Checklist atualizada.'
                        );
                        exibirTarefa(quadro.tarefas.find((a) => a.id === t.id));
                    } catch (erro) {
                        input.checked = item.concluido;
                        feedback.className = 'mensagem erro';
                        feedback.textContent = erro.message;
                        box.append(criarElemento('p', erro.message, 'mensagem erro'));
                    } finally {
                        box.querySelectorAll('input').forEach((i) => {
                            i.disabled = false;
                        });
                    }
                });
                label.append(input, criarElemento('span', item.texto));
                box.append(label);
            });
            conteudo.append(box);
        }
        const rodape = criarElemento('div', undefined, 'kb-preview-acoes');
        const acoes = criarElemento('div');
        acoes.append(
            botao('Mover tarefa', () => abrirMover(t), 'secundario'),
            botao('Editar tarefa', () => editarTarefa(t))
        );
        rodape.append(
            botao('Excluir tarefa', () => confirmarExclusao(t), 'perigo'),
            acoes
        );
        conteudo.append(rodape);
    }
    function configurarEnvioFormulario(form, acao, mensagem) {
        form.addEventListener('submit', async (evento) => {
            evento.preventDefault();
            evento.stopPropagation();
            const submit = form.querySelector('[type="submit"]');
            const rotulo = submit.textContent;
            form.querySelector('[data-kb-erro]').hidden = true;
            submit.disabled = true;
            submit.textContent = 'Salvando…';
            try {
                await salvarAlteracao(acao, mensagem);
                modal.close();
            } catch (erro) {
                erroForm(form, erro.message);
            } finally {
                submit.disabled = false;
                submit.textContent = rotulo;
            }
        });
    }
    function editarTarefa(t, colunaId) {
        if (!quadro || ocupado) {
            return;
        }
        if (!quadro.processos.length) {
            abrir('Cadastre um processo primeiro', 'ORGANIZE O TRABALHO');
            conteudo.append(
                criarElemento('p', 'Cada tarefa pertence a um processo. Cadastre um processo para começar.')
            );
            const link = criarElemento('a', 'Ir para processos', 'botao');
            link.href = '/painel/processos';
            conteudo.append(link);
            return;
        }
        abrir(t ? 'Editar tarefa' : 'Nova tarefa', 'UM PASSO DE CADA VEZ');
        conteudo.append(document.getElementById('kb-form-tarefa').content.cloneNode(true));
        const form = conteudo.querySelector('form');
        quadro.processos.forEach((p) =>
            opcao(form.elements.numeroProcesso, p.numero, p.numero + ' · ' + p.cliente)
        );
        quadro.colunas.forEach((c) => opcao(form.elements.colunaId, c.id, c.nome));
        advogados.forEach((a) =>
            opcao(form.elements.responsavelId, a.id, a.nome + (a.status === 'ATIVO' ? '' : ' · Desativado'))
        );
        if (t) {
            Array.from(form.elements).forEach((campo) => {
                if (campo.name && t[campo.name] !== undefined) {
                    campo.value = t[campo.name] ?? '';
                }
            });
        } else {
            form.elements.numeroProcesso.value = processo.value;
            if (colunaId) {
                form.elements.colunaId.value = colunaId;
            }
        }
        const itens = (t?.checklist || []).map((i) => ({ ...i }));
        const box = form.querySelector('[data-kb-checklist-editor]');
        function renderItens() {
            box.replaceChildren();
            itens.forEach((item, index) => {
                const linha = criarElemento('div', undefined, 'kb-check-editor');
                const check = criarElemento('input');
                check.type = 'checkbox';
                check.checked = item.concluido;
                check.setAttribute('aria-label', 'Item ' + (index + 1) + ' concluído');
                check.addEventListener('change', () => {
                    item.concluido = check.checked;
                });
                const texto = criarElemento('input');
                texto.type = 'text';
                texto.required = true;
                texto.maxLength = 200;
                texto.value = item.texto;
                texto.placeholder = 'O que precisa ser feito?';
                texto.setAttribute('aria-label', 'Texto do item ' + (index + 1));
                texto.addEventListener('input', () => {
                    item.texto = texto.value;
                });
                const remover = botao(
                    '×',
                    () => {
                        itens.splice(index, 1);
                        renderItens();
                    },
                    'secundario'
                );
                remover.setAttribute('aria-label', 'Remover item ' + (index + 1));
                linha.append(check, texto, remover);
                box.append(linha);
            });
            form.querySelector('[data-kb-adicionar-item]').disabled = itens.length >= 20;
        }
        form.querySelector('[data-kb-adicionar-item]').addEventListener('click', () => {
            itens.push({ texto: '', concluido: false });
            renderItens();
            box.lastElementChild.querySelector('[type="text"]').focus();
        });
        renderItens();
        configurarEnvioFormulario(
            form,
            () => {
                const dados = Object.fromEntries(new FormData(form));
                dados.colunaId = Number(dados.colunaId);
                dados.responsavelId = dados.responsavelId ? Number(dados.responsavelId) : null;
                dados.vencimento = dados.vencimento || null;
                dados.checklist = itens;
                dados.versao = t?.versao || 0;
                return requisitarApi(
                    '/api/kanban/tarefas' + (t ? '/' + t.id : ''),
                    t ? 'PUT' : 'POST',
                    dados
                );
            },
            t ? 'Tarefa atualizada.' : 'Tarefa criada.'
        );
        form.elements.titulo.focus();
    }
    function abrirMover(t) {
        abrir('Mover tarefa', t.titulo);
        const form = criarElemento('form', undefined, 'foro-form');
        const field = criarElemento('fieldset');
        const label = criarElemento('label', 'Etapa de destino', 'foro-largo');
        const select = criarElemento('select');
        quadro.colunas.forEach((c) => opcao(select, c.id, c.nome));
        select.value = t.colunaId;
        label.append(select);
        const labelPos = criarElemento('label', 'Posição na coluna', 'foro-largo');
        const pos = criarElemento('select');
        labelPos.append(pos);
        function posicoes() {
            pos.replaceChildren();
            opcao(pos, '', 'No final da coluna');
            quadro.tarefas
                .filter((a) => a.colunaId === Number(select.value) && a.id !== t.id)
                .sort((a, b) => a.ordem - b.ordem)
                .forEach((a) => opcao(pos, a.id, 'Antes de: ' + a.titulo));
        }
        select.addEventListener('change', posicoes);
        posicoes();
        field.append(label, labelPos);
        form.append(field);
        const erro = criarElemento('p', '', 'mensagem erro');
        erro.dataset.kbErro = '';
        erro.hidden = true;
        erro.setAttribute('role', 'alert');
        form.append(erro);
        const rodape = criarElemento('div', undefined, 'foro-form-rodape');
        const submit = criarElemento('button', 'Mover tarefa');
        submit.type = 'submit';
        rodape.append(
            botao('Cancelar', () => modal.close(), 'secundario'),
            submit
        );
        form.append(rodape);
        conteudo.append(form);
        configurarEnvioFormulario(
            form,
            () =>
                requisitarApi('/api/kanban/tarefas/' + t.id + '/mover', 'PUT', {
                    colunaId: Number(select.value),
                    antesDeId: pos.value ? Number(pos.value) : null,
                    versao: t.versao
                }),
            'Tarefa movida.'
        );
    }
    function confirmarExclusao(t) {
        abrir('Excluir tarefa', 'CONFIRME A EXCLUSÃO');
        conteudo.append(criarElemento('p', 'Excluir “' + t.titulo + '” e sua checklist?'));
        const erro = criarElemento('p', '', 'mensagem erro');
        erro.hidden = true;
        erro.setAttribute('role', 'alert');
        conteudo.append(erro);
        const rodape = criarElemento('div', undefined, 'foro-form-rodape');
        const excluir = botao(
            'Excluir tarefa',
            async () => {
                excluir.disabled = true;
                try {
                    await salvarAlteracao(
                        () => requisitarApi('/api/kanban/tarefas/' + t.id + '?versao=' + t.versao, 'DELETE'),
                        'Tarefa excluída.'
                    );
                    modal.close();
                } catch (falha) {
                    erro.textContent = falha.message;
                    erro.hidden = false;
                } finally {
                    excluir.disabled = false;
                }
            },
            'perigo'
        );
        rodape.append(
            botao('Cancelar', () => modal.close(), 'secundario'),
            excluir
        );
        conteudo.append(rodape);
    }
    function iniciarArrasto(t, card) {
        arrastando = t;
        card.classList.add('kb-arrastando');
        ignorarClique = true;
    }
    function posicionar(x, y) {
        const alvoEl = document.elementFromPoint(x, y);
        const coluna = alvoEl?.closest('.kb-coluna');
        quadroColunas.querySelectorAll('.kb-drop').forEach((c) => c.classList.remove('kb-drop'));
        if (!coluna || !quadroColunas.contains(coluna)) {
            placeholder.remove();
            alvo = null;
            return;
        }
        coluna.classList.add('kb-drop');
        const area = coluna.querySelector('.kb-cartoes');
        const proximo = Array.from(area.querySelectorAll('.kb-cartao')).find(
            (c) =>
                Number(c.dataset.tarefaId) !== arrastando.id &&
                y < c.getBoundingClientRect().top + c.getBoundingClientRect().height / 2
        );
        if (placeholder.parentElement !== area || placeholder.nextElementSibling !== (proximo || null)) {
            area.insertBefore(placeholder, proximo || null);
        }
        alvo = {
            colunaId: Number(coluna.dataset.colunaId),
            antesDeId: proximo ? Number(proximo.dataset.tarefaId) : null
        };
        const r = quadroColunas.getBoundingClientRect();
        if (x > r.right - 45) {
            quadroColunas.scrollLeft += 18;
        } else if (x < r.left + 45) {
            quadroColunas.scrollLeft -= 18;
        }
        if (y > window.innerHeight - 60) {
            window.scrollBy(0, 15);
        } else if (y < 90) {
            window.scrollBy(0, -15);
        }
    }
    function finalizarArrasto() {
        placeholder.remove();
        previaArrasto?.remove();
        previaArrasto = null;
        toque = null;
        arrastando = null;
        alvo = null;
        quadroColunas
            .querySelectorAll('.kb-arrastando, .kb-drop')
            .forEach((c) => c.classList.remove('kb-arrastando', 'kb-drop'));
        setTimeout(() => {
            ignorarClique = false;
        }, 300);
    }
    async function soltar() {
        const tarefa = arrastando;
        const destino = alvo;
        finalizarArrasto();
        if (!tarefa || !destino) {
            return;
        }
        try {
            await salvarAlteracao(
                () =>
                    requisitarApi('/api/kanban/tarefas/' + tarefa.id + '/mover', 'PUT', {
                        ...destino,
                        versao: tarefa.versao
                    }),
                'Tarefa movida.'
            );
        } catch (erro) {
            feedback.className = 'mensagem erro';
            feedback.textContent = erro.message;
        }
    }
    quadroColunas.addEventListener('dragenter', (evento) => {
        if (arrastando) {
            evento.preventDefault();
        }
    });
    quadroColunas.addEventListener('dragover', (evento) => {
        if (!arrastando) {
            return;
        }
        evento.preventDefault();
        evento.dataTransfer.dropEffect = 'move';
        posicionar(evento.clientX, evento.clientY);
    });
    quadroColunas.addEventListener('drop', (evento) => {
        if (!arrastando) {
            return;
        }
        evento.preventDefault();
        soltar();
    });
    quadroColunas.addEventListener('pointermove', (evento) => {
        if (!toque || evento.pointerId !== toque.id) {
            return;
        }
        if (!arrastando && Math.hypot(evento.clientX - toque.x, evento.clientY - toque.y) > 8) {
            iniciarArrasto(toque.t, toque.card);
            previaArrasto = toque.card.cloneNode(true);
            previaArrasto.className = 'kb-cartao kb-ghost';
            previaArrasto.removeAttribute('draggable');
            document.body.append(previaArrasto);
        }
        if (arrastando) {
            evento.preventDefault();
            previaArrasto.style.left = evento.clientX - 60 + 'px';
            previaArrasto.style.top = evento.clientY - 30 + 'px';
            posicionar(evento.clientX, evento.clientY);
        }
    });
    quadroColunas.addEventListener('pointerup', (evento) => {
        if (toque && evento.pointerId === toque.id) {
            arrastando ? soltar() : finalizarArrasto();
        }
    });
    quadroColunas.addEventListener('pointercancel', () => {
        if (toque) {
            finalizarArrasto();
        }
    });
    function configurar() {
        if (!quadro || ocupado || !chefe) {
            return;
        }
        abrir('Seu quadro, seu fluxo.', 'CONFIGURAÇÃO DAS COLUNAS', true);
        const revisao = quadro.revisao;
        const colunas = quadro.colunas.map((c) => ({ ...c }));
        const removidas = [];
        const destinos = {};
        conteudo.append(
            criarElemento(
                'p',
                'Personalize nomes e cores. Arraste pelo ícone ⠿ ou use as setas para escolher a ordem. As alterações valem para o quadro de todos os processos.',
                'apoio'
            )
        );
        conteudo.append(
            criarElemento(
                'p',
                'Marque “Conclui tarefas” nas etapas finais. Seus cartões passam a contar como concluídos e deixam de aparecer como vencidos.',
                'apoio'
            )
        );
        const form = criarElemento('form');
        const grade = criarElemento('div', undefined, 'kb-config-lista');
        const boxRemovidas = criarElemento('div');
        const erro = criarElemento('p', '', 'mensagem erro');
        erro.dataset.kbErro = '';
        erro.hidden = true;
        erro.setAttribute('role', 'alert');
        let dragIndex = null;
        function mudarOrdem(origem, destino) {
            if (destino < 0 || destino >= colunas.length) {
                return;
            }
            const [c] = colunas.splice(origem, 1);
            colunas.splice(destino, 0, c);
            desenhar();
        }
        function desenhar() {
            grade.replaceChildren();
            colunas.forEach((c, index) => {
                const linha = criarElemento('div', undefined, 'kb-config-linha');
                linha.dataset.configIndice = index;
                linha.style.setProperty('--kb-cor', c.cor);
                const handle = criarElemento('span', '⠿', 'kb-config-handle');
                handle.draggable = true;
                handle.title = 'Arrastar coluna';
                handle.setAttribute('aria-hidden', 'true');
                handle.addEventListener('dragstart', (evento) => {
                    dragIndex = index;
                    evento.dataTransfer.setData('text/plain', String(index));
                    evento.dataTransfer.effectAllowed = 'move';
                });
                linha.addEventListener('dragover', (evento) => {
                    if (dragIndex !== null) {
                        evento.preventDefault();
                    }
                });
                linha.addEventListener('drop', (evento) => {
                    if (dragIndex !== null) {
                        evento.preventDefault();
                        mudarOrdem(dragIndex, index);
                        dragIndex = null;
                    }
                });
                handle.addEventListener('dragend', () => {
                    dragIndex = null;
                });
                const cor = criarElemento('input');
                cor.type = 'color';
                cor.value = c.cor;
                cor.setAttribute('aria-label', 'Cor da coluna ' + (index + 1));
                cor.addEventListener('input', () => {
                    c.cor = cor.value;
                    linha.style.setProperty('--kb-cor', c.cor);
                });
                const nome = criarElemento('input');
                nome.type = 'text';
                nome.required = true;
                nome.maxLength = 60;
                nome.value = c.nome;
                nome.setAttribute('aria-label', 'Nome da coluna ' + (index + 1));
                nome.addEventListener('input', () => {
                    c.nome = nome.value;
                });
                const label = criarElemento('label');
                const final = criarElemento('input');
                final.type = 'checkbox';
                final.checked = c.conclusiva;
                final.addEventListener('change', () => {
                    c.conclusiva = final.checked;
                });
                label.append(final, criarElemento('span', 'Conclui tarefas'));
                const ordem = criarElemento('div', undefined, 'kb-config-ordem');
                const subir = botao('↑', () => mudarOrdem(index, index - 1), 'secundario');
                const descer = botao('↓', () => mudarOrdem(index, index + 1), 'secundario');
                const remover = botao(
                    '×',
                    () => {
                        if (colunas.length <= 1) {
                            erroForm(form, 'Mantenha pelo menos uma coluna no quadro.');
                            return;
                        }
                        colunas.splice(index, 1);
                        if (c.id) {
                            removidas.push(c);
                        }
                        desenhar();
                    },
                    'perigo'
                );
                subir.disabled = index === 0;
                descer.disabled = index === colunas.length - 1;
                subir.setAttribute('aria-label', 'Mover coluna ' + (index + 1) + ' para a esquerda');
                descer.setAttribute('aria-label', 'Mover coluna ' + (index + 1) + ' para a direita');
                remover.setAttribute('aria-label', 'Remover coluna ' + (index + 1));
                ordem.append(subir, descer, remover);
                linha.append(handle, cor, nome, label, ordem);
                grade.append(linha);
            });
            boxRemovidas.replaceChildren();
            removidas.forEach((c, index) => {
                const quantidade = quadro.tarefas.filter((t) => t.colunaId === c.id).length;
                const div = criarElemento('div', undefined, 'kb-config-removida');
                div.append(criarElemento('strong', 'Remover “' + c.nome + '” · ' + quantidade + ' tarefa(s)'));
                if (quantidade) {
                    const label = criarElemento('label', 'Transferir tarefas para');
                    const select = criarElemento('select');
                    select.required = true;
                    opcao(select, '', 'Escolha uma coluna existente');
                    colunas.filter((a) => a.id).forEach((a) => opcao(select, a.id, a.nome));
                    select.value = destinos[c.id] || '';
                    select.addEventListener('change', () => {
                        destinos[c.id] = select.value ? Number(select.value) : null;
                    });
                    label.append(select);
                    div.append(label);
                }
                div.append(
                    botao(
                        'Desfazer remoção',
                        () => {
                            removidas.splice(index, 1);
                            colunas.splice(Math.min(c.ordem, colunas.length), 0, c);
                            delete destinos[c.id];
                            desenhar();
                        },
                        'secundario'
                    )
                );
                boxRemovidas.append(div);
            });
            adicionar.disabled = colunas.length >= 12;
        }
        const adicionar = botao(
            '+ Adicionar coluna',
            () => {
                colunas.push({ id: null, nome: 'Nova etapa', cor: '#14b8a6', conclusiva: false });
                desenhar();
                grade.lastElementChild.querySelector('[type="text"]').select();
            },
            'secundario'
        );
        const rodape = criarElemento('div', undefined, 'foro-form-rodape');
        const submit = criarElemento('button', 'Salvar configuração');
        submit.type = 'submit';
        rodape.append(
            botao('Cancelar', () => modal.close(), 'secundario'),
            submit
        );
        form.append(grade, adicionar, boxRemovidas, erro, rodape);
        conteudo.append(form);
        desenhar();
        configurarEnvioFormulario(
            form,
            () =>
                requisitarApi('/api/kanban/colunas', 'PUT', {
                    colunas: colunas.map((c) => ({
                        id: c.id,
                        nome: c.nome,
                        cor: c.cor,
                        conclusiva: c.conclusiva
                    })),
                    destinos,
                    revisao
                }),
            'Configuração do quadro salva.'
        );
    }
    function limpar() {
        processo.value = '';
        busca.value = '';
        responsavel.value = '';
        prioridade.value = '';
        vencidas.checked = false;
        atualizarFiltroProcesso();
    }
    function atualizarFiltroProcesso() {
        const url = new window.URL(window.location.href);
        processo.value
            ? url.searchParams.set('processo', processo.value)
            : url.searchParams.delete('processo');
        window.history.replaceState(null, '', url);
        renderizar();
    }
    processo.addEventListener('change', atualizarFiltroProcesso);
    busca.addEventListener('input', renderizar);
    [responsavel, prioridade, vencidas].forEach((e) => e.addEventListener('change', renderizar));
    document.getElementById('kb-limpar').addEventListener('click', limpar);
    document.getElementById('kb-atualizar').addEventListener('click', carregar);
    document.getElementById('kb-nova').addEventListener('click', () => editarTarefa());
    document.getElementById('kb-configurar')?.addEventListener('click', configurar);
    modal.addEventListener('click', (evento) => {
        if (ocupado) {
            return;
        }
        if (evento.target.closest('[data-kb-fechar]')) {
            modal.close();
            return;
        }
        if (evento.target === modal) {
            const r = modal.getBoundingClientRect();
            if (
                evento.clientX < r.left ||
                evento.clientX > r.right ||
                evento.clientY < r.top ||
                evento.clientY > r.bottom
            ) {
                modal.close();
            }
        }
    });
    modal.addEventListener('cancel', (evento) => {
        if (ocupado) {
            evento.preventDefault();
        }
    });
    carregar();
})();
