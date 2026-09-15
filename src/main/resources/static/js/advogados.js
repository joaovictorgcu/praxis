angular.module("advogadosApp", [])
    .directive("praxisAlerta", ["$timeout", function ($timeout) {
        return {
            restrict: "E",
            scope: {
                tipo: "@",
                mensagem: "@",
                duracao: "@",
                aoFechar: "&"
            },
            template:
                '<div class="praxis-alerta" ng-class="\'praxis-alerta--\' + tipo">' +
                    '<span class="praxis-alerta__icone" ng-bind="icone"></span>' +
                    '<span class="praxis-alerta__texto" ng-bind="mensagem"></span>' +
                    '<button type="button" class="praxis-alerta__fechar" ng-click="fechar()" aria-label="Fechar">&times;</button>' +
                '</div>',
            link: function (scope) {
                var icones = { success: "✓", danger: "✖", warning: "⚠" };
                scope.icone = icones[scope.tipo] || icones.success;

                var temporizador = $timeout(fechar, parseInt(scope.duracao, 10) || 4000);

                scope.fechar = fechar;

                function fechar() {
                    $timeout.cancel(temporizador);
                    scope.aoFechar();
                }
            }
        };
    }])
    .controller("AdvogadosController", ["$http", "$document", "$rootScope", "$timeout", function ($http, $document, $rootScope, $timeout) {
        var vm = this;

        vm.advogados = [];
        vm.advogadosPaginados = [];
        vm.novo = {};
        vm.modalAberto = false;
        vm.modoEdicao = false;
        vm.enviando = false;
        vm.erro = null;
        vm.alertas = [];
        vm.menuAberto = null;
        vm.itensPorPagina = 10;
        vm.paginaAtual = 1;
        vm.totalPaginas = 1;
        vm.termoBusca = "";
        vm.ordenacao = { campo: null, direcao: 1 };

        var proximoIdAlerta = 1;
        var debounceBusca = null;

        vm.notificar = function (tipo, mensagem) {
            vm.alertas.push({ id: proximoIdAlerta++, tipo: tipo, mensagem: mensagem });
        };

        vm.removerAlerta = function (id) {
            vm.alertas = vm.alertas.filter(function (alerta) {
                return alerta.id !== id;
            });
        };

        function atualizarPaginacao() {
            vm.totalPaginas = Math.max(1, Math.ceil(vm.advogados.length / vm.itensPorPagina));
            if (vm.paginaAtual > vm.totalPaginas) {
                vm.paginaAtual = vm.totalPaginas;
            }
            var inicio = (vm.paginaAtual - 1) * vm.itensPorPagina;
            vm.advogadosPaginados = vm.advogados.slice(inicio, inicio + vm.itensPorPagina);
        }

        vm.carregar = function () {
            var termo = (vm.termoBusca || "").trim();
            var params = termo.length >= 2 ? { busca: termo } : {};

            $http.get("/api/advogados", { params: params }).then(function (resposta) {
                vm.advogados = resposta.data;
                vm.ordenacao = { campo: null, direcao: 1 };
                atualizarPaginacao();
            });
        };

        vm.aoDigitarBusca = function () {
            if (debounceBusca) {
                $timeout.cancel(debounceBusca);
            }

            debounceBusca = $timeout(function () {
                var termo = (vm.termoBusca || "").trim();
                if (termo.length >= 2 || termo.length === 0) {
                    vm.paginaAtual = 1;
                    vm.carregar();
                }
            }, 300);
        };

        vm.ordenarPor = function (campo) {
            if (vm.ordenacao.campo === campo) {
                vm.ordenacao.direcao *= -1;
            } else {
                vm.ordenacao.campo = campo;
                vm.ordenacao.direcao = 1;
            }

            vm.advogadosPaginados = vm.advogadosPaginados.slice().sort(function (a, b) {
                var valorA = (a[campo] === null || a[campo] === undefined) ? "" : a[campo].toString().toLowerCase();
                var valorB = (b[campo] === null || b[campo] === undefined) ? "" : b[campo].toString().toLowerCase();

                if (valorA < valorB) {
                    return -1 * vm.ordenacao.direcao;
                }
                if (valorA > valorB) {
                    return 1 * vm.ordenacao.direcao;
                }
                return 0;
            });
        };

        vm.irParaPagina = function (pagina) {
            if (pagina < 1 || pagina > vm.totalPaginas) {
                return;
            }
            vm.paginaAtual = pagina;
            atualizarPaginacao();
        };

        vm.paginaAnterior = function () {
            vm.irParaPagina(vm.paginaAtual - 1);
        };

        vm.proximaPagina = function () {
            vm.irParaPagina(vm.paginaAtual + 1);
        };

        vm.numerosDePagina = function () {
            var janela = 2;
            var inicio = Math.max(1, vm.paginaAtual - janela);
            var fim = Math.min(vm.totalPaginas, vm.paginaAtual + janela);
            var numeros = [];

            for (var i = inicio; i <= fim; i++) {
                numeros.push(i);
            }
            return numeros;
        };

        vm.abrirModal = function () {
            vm.novo = {};
            vm.erro = null;
            vm.modoEdicao = false;
            vm.modalAberto = true;
        };

        vm.editar = function (advogado) {
            vm.menuAberto = null;
            vm.erro = null;

            $http.get("/api/advogados/" + advogado.id).then(function (resposta) {
                vm.novo = angular.copy(resposta.data);
                vm.modoEdicao = true;
                vm.modalAberto = true;
            });
        };

        vm.fecharModal = function () {
            vm.modalAberto = false;
        };

        vm.formatarTelefone = function () {
            var digitos = (vm.novo.telefone || "").replace(/\D/g, "").slice(0, 11);

            if (digitos.length === 0) {
                vm.novo.telefone = "";
            } else if (digitos.length <= 2) {
                vm.novo.telefone = "(" + digitos;
            } else if (digitos.length <= 6) {
                vm.novo.telefone = "(" + digitos.slice(0, 2) + ") " + digitos.slice(2);
            } else if (digitos.length <= 10) {
                vm.novo.telefone = "(" + digitos.slice(0, 2) + ") " + digitos.slice(2, 6) + "-" + digitos.slice(6);
            } else {
                vm.novo.telefone = "(" + digitos.slice(0, 2) + ") " + digitos.slice(2, 7) + "-" + digitos.slice(7);
            }
        };

        vm.salvar = function () {
            vm.enviando = true;
            vm.erro = null;

            var requisicao = vm.modoEdicao
                ? $http.put("/api/advogados/" + vm.novo.id, vm.novo)
                : $http.post("/api/advogados", vm.novo);

            requisicao.then(function (resposta) {
                vm.notificar("success", "Advogado " + resposta.data.nome + " salvo com a OAB " + resposta.data.oab + ".");
                vm.modalAberto = false;
                vm.enviando = false;
                vm.carregar();
            }, function (resposta) {
                vm.erro = (resposta.data && resposta.data.erro) || "Nao foi possivel salvar o advogado.";
                vm.enviando = false;
            });
        };

        vm.alternarMenu = function (id, evento) {
            evento.stopPropagation();
            vm.menuAberto = vm.menuAberto === id ? null : id;
        };

        vm.alternarStatus = function (advogado) {
            vm.menuAberto = null;
            var acao = advogado.status === "ATIVO" ? "desativar" : "ativar";

            if (!window.confirm("Deseja " + acao + " o advogado " + advogado.nome + "?")) {
                return;
            }

            $http.post("/api/advogados/" + advogado.id + "/" + acao).then(function () {
                vm.notificar("success", "Advogado " + advogado.nome + (acao === "ativar" ? " ativado." : " desativado."));
                vm.carregar();
            }, function () {
                vm.notificar("danger", "Nao foi possivel " + acao + " o advogado " + advogado.nome + ".");
            });
        };

        $document.on("click", function () {
            if (vm.menuAberto !== null) {
                vm.menuAberto = null;
                $rootScope.$applyAsync();
            }
        });

        vm.carregar();
    }]);
