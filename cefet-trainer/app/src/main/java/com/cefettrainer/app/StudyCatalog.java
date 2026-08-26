package com.cefettrainer.app;

import java.util.*;

/**
 * Conteúdo pedagógico do tutor remasterizado.
 *
 * Princípios usados aqui:
 * - conceitos pequenos e encadeados por pré-requisitos;
 * - linguagem inicial curta, sem reduzir a dificuldade conceitual;
 * - exemplo resolvido antes de cobrar transferência;
 * - feedback específico por alternativa;
 * - uma resposta errada pode apontar para a lacuna anterior exata.
 */
public final class StudyCatalog {
    private StudyCatalog() {}

    public static final class Question {
        public final String prompt;
        public final String[] options;
        public final String[] feedback;
        public final String[] route;
        public final int correct;

        Question(String prompt, int correct, String[] options, String[] feedback, String[] route) {
            this.prompt = prompt;
            this.correct = correct;
            this.options = options;
            this.feedback = feedback;
            this.route = route;
        }
    }

    public static final class Node {
        public final String id;
        public final String subject;
        public final String title;
        public final int level;
        public final String summary;
        public final String memory;
        public final String terms;
        public final String detail;
        public final String worked;
        public final String counterExample;
        public final String trap;
        public final String[] prerequisites;
        public final Question check;
        public final Question transfer;

        Node(String id, String subject, String title, int level, String summary, String memory,
             String terms, String detail, String worked, String counterExample, String trap,
             String[] prerequisites, Question check, Question transfer) {
            this.id = id;
            this.subject = subject;
            this.title = title;
            this.level = level;
            this.summary = summary;
            this.memory = memory;
            this.terms = terms;
            this.detail = detail;
            this.worked = worked;
            this.counterExample = counterExample;
            this.trap = trap;
            this.prerequisites = prerequisites == null ? new String[0] : prerequisites;
            this.check = check;
            this.transfer = transfer;
        }
    }

    public static final String[] SUBJECT_ORDER = {
            "Português", "Matemática", "Física", "Química", "Biologia", "História", "Geografia"
    };

    public static final String[] FOUNDATION = {
            "p_palavra_frase", "p_substantivo", "p_artigo", "p_adjetivo", "p_verbo", "p_numero", "p_sujeito", "p_predicado",
            "p_info_explicita", "p_inferencia",
            "m_operacoes", "m_inteiros", "m_fracao", "m_fracao_operacoes", "m_decimal", "m_razao", "m_porcentagem",
            "m_distributiva", "m_algebra", "m_equacao1", "m_sistema1", "m_equacao2", "m_funcao",
            "f_unidades", "f_velocidade", "q_materia", "q_atomo", "b_celula", "h_tempo_fonte", "g_mapa_escala"
    };

    public static LinkedHashMap<String, Node> create() {
        LinkedHashMap<String, Node> m = new LinkedHashMap<>();

        // ========================= PORTUGUÊS =========================
        add(m, n(
                "p_palavra_frase", "Português", "Palavra, frase, oração e período", 1,
                "Palavra é uma unidade da língua. Frase é um enunciado com sentido. Oração é uma frase ou parte de frase organizada em torno de um verbo. Período é um enunciado formado por uma ou mais orações.",
                "PALAVRA = peça.\nFRASE = mensagem.\nORAÇÃO = tem verbo.\nPERÍODO = uma ou mais orações.",
                "enunciado = aquilo que foi dito ou escrito com intenção de comunicar\nverbo = palavra que pode indicar ação, estado, mudança ou fenômeno\noração = estrutura organizada em torno de verbo ou locução verbal",
                "Nem toda frase tem verbo. 'Silêncio!' é frase, mas não é oração. 'Os alunos chegaram.' tem o verbo 'chegaram', então é oração e também é um período simples. 'Os alunos chegaram e abriram os cadernos.' possui dois verbos nucleares: chegaram / abriram; portanto há duas orações no mesmo período.",
                "Texto: 'Que frio! A janela ficou aberta.' A primeira parte comunica uma reação, mas não traz verbo. A segunda traz 'ficou'. Logo: duas frases; apenas a segunda é oração.",
                "Contraexemplo: dizer que toda sequência que termina com ponto é automaticamente uma oração. A pontuação não cria verbo.",
                "Erro comum: procurar sujeito antes de localizar o verbo. Em análise de oração, achar o verbo primeiro costuma simplificar o resto.",
                null,
                q("Qual trecho é uma oração?", 2,
                        "Atenção!", "É frase com sentido, mas não há verbo.", null,
                        "Que surpresa!", "Também é frase sem verbo.", null,
                        "A turma terminou a prova.", "Correto: 'terminou' é verbo; o trecho está organizado em torno dele.", null,
                        "Boa noite!", "Há comunicação, mas nenhum verbo.", null),
                q("No período 'O sinal tocou, mas alguns candidatos continuaram escrevendo', quantas orações há?", 1,
                        "Uma", "Há mais de um núcleo verbal: 'tocou' e 'continuaram escrevendo'.", "p_verbo",
                        "Duas", "Correto. 'tocou' forma uma oração e 'continuaram escrevendo' forma outra.", null,
                        "Três", "'continuaram escrevendo' funciona como uma locução verbal, não como duas orações separadas aqui.", "p_verbo",
                        "Nenhuma", "O período contém verbos claros, então contém orações.", "p_verbo")));

        add(m, n(
                "p_substantivo", "Português", "Substantivo: o nome dos seres, coisas, lugares e ideias", 1,
                "Substantivo é a palavra usada como nome. Pode nomear pessoa, lugar, objeto, sentimento, acontecimento, instituição ou ideia.",
                "Se a palavra está funcionando como NOME, pense primeiro em substantivo.",
                "substantivo comum = nome geral: cidade, aluno\nsubstantivo próprio = nome individual: Rio de Janeiro, Ana\nconcreto = nomeia ser concebido como existente por si: pedra, fada\nabstrato = nomeia ação, estado, qualidade ou sentimento: corrida, beleza, medo\ncoletivo = singular que nomeia conjunto: cardume, alcateia",
                "A classificação depende do uso e do sentido. 'Brasil' é próprio; 'país' é comum. 'Tristeza' é abstrato porque nomeia um estado. Substantivos variam frequentemente em número: aluno/alunos. Alguns também variam em gênero: menino/menina. Essas variações ajudam a construir concordância com artigos e adjetivos.",
                "Na frase 'A rápida expansão da cidade mudou a paisagem', os substantivos são 'expansão', 'cidade' e 'paisagem'. 'rápida' caracteriza expansão; 'mudou' é verbo.",
                "Contraexemplo: achar que uma palavra é substantivo porque 'parece nome'. Em 'o jantar terminou', jantar é substantivo; em 'vamos jantar', jantar funciona como verbo.",
                "Erro comum: confundir substantivo com adjetivo. Pergunte: a palavra NOMEIA algo ou CARACTERIZA algo?",
                new String[]{"p_palavra_frase"},
                q("Em 'A coragem do bombeiro impressionou a equipe', qual palavra é um substantivo abstrato?", 0,
                        "coragem", "Correto: nomeia uma qualidade/estado, não um objeto físico.", null,
                        "bombeiro", "É substantivo, mas concreto: nomeia uma pessoa.", null,
                        "impressionou", "É verbo; indica o acontecimento expresso pela oração.", "p_verbo",
                        "a", "É artigo; acompanha um substantivo.", "p_artigo"),
                q("Em qual alternativa a mesma palavra muda de classe conforme o contexto?", 2,
                        "A escola abriu / A escola fechou", "'escola' continua funcionando como substantivo nos dois casos.", null,
                        "O livro caiu / O livro sumiu", "'livro' continua substantivo.", null,
                        "O jantar atrasou / Vamos jantar cedo", "Correto: no primeiro caso, 'jantar' nomeia uma refeição; no segundo, expressa uma ação.", null,
                        "A chuva parou / A chuva voltou", "'chuva' continua substantivo.", null)));

        add(m, n(
                "p_artigo", "Português", "Artigo: a palavra que acompanha o substantivo", 1,
                "Artigo vem antes de um substantivo e ajuda a mostrar se ele está definido ou indefinido, além de marcar gênero e número em muitos casos.",
                "O/A/OS/AS = definidos.\nUM/UMA/UNS/UMAS = indefinidos.",
                "definido = apresenta o referente como identificável: 'o aluno'\nindefinido = apresenta de modo não identificado: 'um aluno'\ngênero = masculino/feminino gramatical\nnúmero = singular/plural",
                "Compare: 'Um candidato chamou o fiscal' introduz um candidato ainda não identificado. 'O candidato explicou o problema' retoma alguém identificável. O artigo também sinaliza número: 'a questão' / 'as questões'. Isso será importante para concordância nominal.",
                "Em 'Uma pesquisadora encontrou o documento', 'uma' acompanha pesquisadora; 'o' acompanha documento. Os dois são artigos, mas o primeiro é indefinido e o segundo definido.",
                "Contraexemplo: todo 'um' é artigo. Em 'Tenho um só objetivo', pode ser numeral dependendo do sentido de quantidade exata; a função precisa ser observada no contexto.",
                "Erro comum: decorar a lista e esquecer a função. Procure qual substantivo o termo está determinando.",
                new String[]{"p_substantivo"},
                q("Em 'Os estudantes revisaram uma questão difícil', quais são os artigos?", 1,
                        "estudantes e questão", "São substantivos, não artigos.", "p_substantivo",
                        "os e uma", "Correto: 'os' acompanha estudantes e 'uma' acompanha questão.", null,
                        "revisaram e difícil", "'revisaram' é verbo e 'difícil' é adjetivo.", "p_verbo",
                        "uma e difícil", "'uma' é artigo; 'difícil' caracteriza o substantivo questão.", "p_adjetivo"),
                q("A troca de 'um pesquisador' por 'o pesquisador' altera principalmente qual informação?", 2,
                        "O tempo verbal", "O verbo não foi alterado.", "p_verbo",
                        "O gênero da palavra pesquisador", "O gênero permanece masculino.", "p_numero",
                        "O grau de identificação do referente", "Correto: o artigo definido sugere que o leitor consegue identificar qual pesquisador é.", null,
                        "A classe de 'pesquisador'", "'pesquisador' continua substantivo.", "p_substantivo")));

        add(m, n(
                "p_adjetivo", "Português", "Adjetivo: característica ou estado ligado a um nome", 1,
                "Adjetivo é a palavra que atribui característica, estado, origem ou qualidade a um substantivo.",
                "SUBSTANTIVO nomeia. ADJETIVO caracteriza.",
                "qualidade = traço atribuído: cuidadoso\nestado = condição: cansado\nlocução adjetiva = grupo de palavras com valor de adjetivo: 'de escola' em 'material de escola'",
                "O mesmo adjetivo pode mudar de sentido conforme o substantivo. 'grande homem' pode indicar importância; 'homem grande' tende a indicar tamanho. A posição também pode produzir efeito de sentido, por isso a prova pode cobrar mais que identificação mecânica.",
                "Em 'A análise cuidadosa evitou um erro grave', 'cuidadosa' caracteriza análise e 'grave' caracteriza erro. Eles não nomeiam novos seres: acrescentam propriedades.",
                "Contraexemplo: 'O jovem chegou'. Aqui 'jovem' pode funcionar como substantivo porque está nomeando a pessoa. Classe gramatical depende da função no contexto.",
                "Erro comum: procurar adjetivo apenas depois do substantivo. Ele pode aparecer antes ou ser ligado por verbo: 'A prova estava difícil'.",
                new String[]{"p_substantivo"},
                q("Em 'As antigas pontes permanecem resistentes', quais palavras funcionam como adjetivos?", 2,
                        "pontes e resistentes", "'pontes' é substantivo; 'resistentes' é adjetivo.", "p_substantivo",
                        "as e antigas", "'as' é artigo; 'antigas' é adjetivo.", "p_artigo",
                        "antigas e resistentes", "Correto: ambas caracterizam 'pontes'.", null,
                        "permanecem e resistentes", "'permanecem' é verbo; 'resistentes' é adjetivo.", "p_verbo"),
                q("Em qual frase a posição do adjetivo pode mudar o sentido mais claramente?", 1,
                        "A parede branca caiu / A branca parede caiu", "A mudança é mais estilística e menos semântica nesse caso.", null,
                        "Um grande homem falou / Um homem grande falou", "Correto: a primeira expressão pode indicar importância; a segunda, tamanho físico.", null,
                        "A água fria acabou / A fria água acabou", "Há forte diferença de naturalidade, mas não a oposição semântica mais clara.", null,
                        "O caderno novo sumiu / O novo caderno sumiu", "Pode haver nuance, mas a oposição é menos direta que em 'grande homem/homem grande'.", null)));

        add(m, n(
                "p_pronome", "Português", "Pronome: retoma, aponta ou acompanha nomes", 1,
                "Pronome pode substituir um substantivo, retomá-lo, apontá-lo ou acompanhar um nome para indicar pessoa, posse, posição e outras relações.",
                "Pronome evita repetir nomes e conecta partes do texto.",
                "pessoal = eu, tu, ele, nós...\npossessivo = meu, sua, nosso...\ndemonstrativo = este, esse, aquele...\nrelativo = que, quem, cujo, onde...\nreferente = elemento ao qual o pronome se liga",
                "Em textos, pronomes são peças de coesão. Em 'Maria encontrou Joana. Ela estava atrasada', 'ela' precisa de um referente. Se o contexto não deixa claro se é Maria ou Joana, surge ambiguidade. Questões de interpretação exploram justamente essas retomadas.",
                "'O pesquisador publicou o artigo. Ele recebeu críticas.' Para interpretar 'ele', verifique qual termo anterior combina com gênero, número, sentido e contexto.",
                "Contraexemplo: achar que todo 'que' é pronome. 'Disse que viria' tem 'que' como conjunção; 'o livro que comprei' tem pronome relativo.",
                "Erro comum: escolher o referente apenas por proximidade. Compatibilidade sintática e sentido do texto também importam.",
                new String[]{"p_substantivo", "p_numero"},
                q("Em 'As atletas receberam as medalhas e as guardaram', o segundo 'as' retoma o quê?", 2,
                        "as atletas", "Se retomasse atletas, o sentido seria 'guardaram as atletas', o que não combina com o contexto.", null,
                        "receberam", "Verbo não é o referente do pronome objeto aqui.", "p_verbo",
                        "as medalhas", "Correto: elas receberam as medalhas e guardaram as medalhas.", null,
                        "a frase inteira", "O pronome substitui um termo nominal específico nesse caso.", null),
                q("Em 'Carla contou a Beatriz que sua inscrição fora aceita', qual problema de coesão pode existir?", 0,
                        "Ambiguidade sobre de quem é a inscrição", "Correto: 'sua' pode apontar para Carla ou Beatriz sem contexto adicional.", null,
                        "Erro obrigatório de plural", "Não há plural envolvido.", "p_numero",
                        "Ausência de verbo", "Há verbos: contou / fora aceita.", "p_verbo",
                        "Falta de substantivo", "Carla, Beatriz e inscrição são nomes/substantivos.", "p_substantivo")));

        add(m, n(
                "p_verbo", "Português", "Verbo e locução verbal", 1,
                "Verbo é a palavra que organiza a oração e pode expressar ação, estado, mudança, fenômeno ou processo. Locução verbal é um conjunto de verbos que funciona como uma unidade verbal.",
                "Ache o verbo para achar a estrutura da oração.",
                "tempo = quando o processo é apresentado\nmodo = atitude do falante: certeza, hipótese, ordem etc.\npessoa = eu/tu/ele/nós/vós/eles\nlocução verbal = 'vai estudar', 'tinha saído', 'pode chover'",
                "O verbo concorda frequentemente com o sujeito em pessoa e número. Em 'Os candidatos chegaram', 'chegaram' está no plural. Em 'A candidata chegou', está no singular. Em locuções, não conte cada forma verbal como uma oração automaticamente: 'vai estudar' forma um único núcleo verbal nessa análise básica.",
                "Em 'Quando o portão abriu, os estudantes começaram a entrar', há o verbo 'abriu' e a locução 'começaram a entrar'. O período contém duas orações.",
                "Contraexemplo: palavra terminada em -ar/-er/-ir é sempre verbo em uso. 'O jantar esfriou' mostra 'jantar' funcionando como substantivo.",
                "Erro comum: procurar apenas ações físicas. 'ser', 'estar', 'parecer' e 'permanecer' também são verbos.",
                new String[]{"p_palavra_frase"},
                q("Qual alternativa contém uma locução verbal?", 3,
                        "A prova começou", "Há um verbo simples: começou.", null,
                        "O aluno escreveu", "Há um verbo simples: escreveu.", null,
                        "A sala permaneceu silenciosa", "'permaneceu' é um verbo simples.", null,
                        "A equipe pode vencer", "Correto: 'pode vencer' funciona como uma unidade verbal.", null),
                q("No período 'Se chover, a organização poderá adiar a atividade', quais são os núcleos verbais?", 2,
                        "se e a", "São palavras funcionais, não verbos.", null,
                        "chover, organização e atividade", "Organização e atividade são substantivos.", "p_substantivo",
                        "chover e poderá adiar", "Correto: 'chover' forma uma oração e 'poderá adiar' é uma locução verbal na outra.", null,
                        "poderá e adiar, sempre em duas orações", "A locução 'poderá adiar' funciona como um núcleo verbal, não exige duas orações.", null)));

        add(m, n(
                "p_numero", "Português", "Singular e plural: número gramatical", 1,
                "Singular indica uma unidade gramatical. Plural indica mais de uma. Artigos, substantivos, adjetivos, pronomes e verbos podem mostrar essa relação.",
                "SINGULAR = uma unidade.\nPLURAL = mais de uma.",
                "número gramatical = contraste singular/plural\nmarca de plural = sinal na forma da palavra, como -s em muitos casos\nconcordância = palavras relacionadas ajustam suas formas",
                "Não basta procurar a letra S. 'lápis' pode estar no singular ou plural conforme o contexto: 'o lápis' / 'os lápis'. E o plural do grupo aparece em várias palavras: 'as questões difíceis foram anuladas'. Artigo, substantivo, adjetivo e verbo contribuem para a leitura do número.",
                "Compare: 'A questão difícil foi anulada' e 'As questões difíceis foram anuladas'. A ideia passa de uma questão para várias; por isso várias formas mudam juntas.",
                "Contraexemplo: 'ônibus' termina em s, mas pode ser singular: 'o ônibus chegou'.",
                "Erro comum: decidir singular/plural olhando uma palavra isolada quando o contexto resolve a dúvida.",
                new String[]{"p_substantivo", "p_artigo", "p_adjetivo", "p_verbo"},
                q("Qual frase está totalmente no plural?", 1,
                        "As questão difíceis foram anulada.", "Artigo está plural, mas substantivo e parte da concordância não acompanharam.", "p_concordancia_nominal",
                        "As questões difíceis foram anuladas.", "Correto: o grupo nominal e a forma verbal/predicativa aparecem ajustados ao plural.", null,
                        "A questões difícil foram anuladas.", "Há mistura de singular e plural no grupo nominal.", "p_concordancia_nominal",
                        "A questão difícil foram anuladas.", "O sujeito está singular, mas o verbo está plural.", "p_concordancia_verbal"),
                q("Em 'Os lápis novos desapareceram', o que permite concluir que 'lápis' é plural?", 3,
                        "A palavra termina em s", "'lápis' também pode ser singular; a terminação isolada não resolve.", null,
                        "A palavra é substantivo", "Ser substantivo não determina singular ou plural.", "p_substantivo",
                        "O verbo termina em -ram apenas", "Ajuda, mas o grupo nominal também traz pistas diretas.", "p_verbo",
                        "O conjunto 'os' + 'novos' + 'desapareceram' marca a relação plural", "Correto: o contexto sintático inteiro confirma o plural.", null)));

        add(m, n(
                "p_sujeito", "Português", "Sujeito: de quem ou do que a oração declara algo", 2,
                "Sujeito é o termo com o qual o verbo normalmente estabelece concordância e sobre o qual se faz uma declaração na oração.",
                "1º ache o verbo. 2º pergunte: quem/o que está ligado a esse verbo?",
                "núcleo do sujeito = palavra principal do sujeito\nsujeito simples = um núcleo\nsujeito composto = mais de um núcleo\nsujeito oculto = não aparece escrito, mas é recuperável pela forma verbal/contexto",
                "Evite a regra falsa 'sujeito é quem faz a ação'. Em 'A janela foi quebrada pelo vento', 'a janela' é sujeito, embora não seja agente. O teste de concordância é mais confiável: 'As janelas foram quebradas'.",
                "'Os resultados da pesquisa surpreenderam a equipe.' Verbo: surpreenderam. O que surpreendeu? 'Os resultados da pesquisa'. Núcleo: resultados.",
                "Contraexemplo: em 'Choveu muito', não existe um sujeito comum escondido chamado 'a chuva'. Trata-se de oração sem sujeito na análise tradicional.",
                "Erro comum: escolher o substantivo mais próximo do verbo. O núcleo pode estar mais distante.",
                new String[]{"p_verbo", "p_substantivo", "p_numero"},
                q("Em 'As novas regras do edital preocupam alguns candidatos', qual é o núcleo do sujeito?", 1,
                        "novas", "É adjetivo que caracteriza regras.", "p_adjetivo",
                        "regras", "Correto: é a palavra principal do grupo 'As novas regras do edital'.", null,
                        "edital", "Está dentro de uma expressão ligada a 'regras', mas não é o núcleo do sujeito.", "p_substantivo",
                        "candidatos", "É o termo afetado por 'preocupam', não o sujeito.", null),
                q("Em 'Chegaram cedo ao local os fiscais responsáveis', por que 'os fiscais responsáveis' é sujeito?", 2,
                        "Porque vem antes do verbo", "Nesse exemplo, o sujeito aparece depois do verbo.", null,
                        "Porque todo substantivo é sujeito", "Substantivos podem exercer várias funções.", "p_substantivo",
                        "Porque concorda com 'chegaram' e é o termo de que se declara a chegada", "Correto. A ordem invertida não muda a relação sintática.", null,
                        "Porque 'local' não tem plural", "Isso não determina a função de sujeito.", "p_numero")));

        add(m, n(
                "p_predicado", "Português", "Predicado: o que se declara sobre o sujeito", 2,
                "Depois de identificar o sujeito, o predicado é a parte da oração que contém a declaração feita sobre ele e inclui o verbo.",
                "SUJEITO = foco da declaração.\nPREDICADO = declaração feita sobre ele.",
                "predicado verbal = núcleo principal é verbo significativo\npredicado nominal = núcleo de sentido é característica/estado ligado por verbo\npredicativo = termo que atribui característica a sujeito ou objeto",
                "Em 'A prova parecia difícil', sujeito = 'A prova'; predicado = 'parecia difícil'. O verbo 'parecia' liga o sujeito à característica 'difícil'. Em 'A equipe resolveu o problema', o centro da declaração é a ação 'resolveu'.",
                "'Os candidatos permaneceram tranquilos durante a espera.' Sujeito: os candidatos. Predicado: permaneceram tranquilos durante a espera. 'tranquilos' atribui estado ao sujeito.",
                "Contraexemplo: retirar o verbo do predicado. O predicado contém o verbo, mesmo quando seu principal conteúdo semântico é um nome/adjetivo.",
                "Erro comum: dividir a frase simplesmente no meio. A análise depende das relações sintáticas.",
                new String[]{"p_sujeito", "p_verbo", "p_adjetivo"},
                q("Em 'A sala ficou silenciosa após o aviso', qual trecho é o predicado?", 3,
                        "A sala", "Esse é o sujeito.", "p_sujeito",
                        "silenciosa", "É parte do predicado, mas não o predicado inteiro.", "p_adjetivo",
                        "após o aviso", "É apenas uma circunstância dentro da declaração.", null,
                        "ficou silenciosa após o aviso", "Correto: contém o verbo e tudo o que se declara sobre 'A sala'.", null),
                q("Em 'Os alunos resolveram rapidamente a questão', qual é o núcleo do predicado verbal?", 0,
                        "resolveram", "Correto: é o verbo que organiza a ação declarada.", null,
                        "alunos", "É núcleo do sujeito.", "p_sujeito",
                        "rapidamente", "É advérbio; modifica o modo da ação.", null,
                        "questão", "É substantivo que participa do complemento do verbo.", "p_substantivo")));

        add(m, n(
                "p_concordancia_nominal", "Português", "Concordância nominal", 2,
                "Palavras ligadas a um substantivo ajustam gênero e número quando a norma-padrão exige. Artigos e adjetivos são os casos mais visíveis.",
                "Olhe o grupo do substantivo: artigo + nome + característica precisam combinar.",
                "nominal = relativo a nomes\nconcordar = ajustar formas relacionadas\ngênero = masculino/feminino gramatical\nnúmero = singular/plural",
                "Em 'as duas questões difíceis', o substantivo 'questões' está no feminino plural; 'as', 'duas' e 'difíceis' se organizam em torno desse núcleo. Nem toda palavra varia: 'difícil' muda só em número, não em gênero.",
                "'Foram divulgadas novas regras.' 'novas' e 'divulgadas' aparecem no feminino plural por se relacionarem a 'regras'.",
                "Contraexemplo: pensar que toda palavra próxima do substantivo deve receber -s. Preposições e advérbios, por exemplo, não seguem essa lógica.",
                "Erro comum: corrigir uma palavra e esquecer as demais do mesmo grupo.",
                new String[]{"p_substantivo", "p_artigo", "p_adjetivo", "p_numero"},
                q("Qual opção completa corretamente: 'Foram publicadas ___ orientações ___.'?", 1,
                        "novo / importante", "As duas formas estão no singular/masculino e não concordam com 'orientações'.", "p_numero",
                        "novas / importantes", "Correto: ambas acompanham 'orientações', feminino plural.", null,
                        "novos / importantes", "'novos' está masculino e não concorda com 'orientações'.", "p_adjetivo",
                        "nova / importantes", "Há mistura de singular e plural.", "p_numero"),
                q("Em 'É proibida a entrada de pessoas não autorizadas', por que 'proibida' está no feminino singular?", 2,
                        "Porque toda palavra antes de 'entrada' fica feminina", "Posição não determina concordância.", null,
                        "Porque 'pessoas' está no feminino", "A relação principal de 'proibida' é com 'entrada', não com pessoas.", "p_sujeito",
                        "Porque se relaciona com 'a entrada', núcleo feminino singular", "Correto: a concordância segue o termo com o qual a característica se liga.", null,
                        "Porque termina em -a por acaso", "A forma é motivada pela relação gramatical.", null)));

        add(m, n(
                "p_concordancia_verbal", "Português", "Concordância verbal", 2,
                "O verbo normalmente ajusta pessoa e número ao núcleo do sujeito.",
                "Ache o verbo. Ache o sujeito. Faça os dois combinarem.",
                "pessoa verbal = eu/nós, tu/vós, ele/eles etc.\nnúmero = singular/plural\nnúcleo do sujeito = palavra principal que controla a concordância em muitos casos",
                "A distância não muda a regra: 'A lista de candidatos aprovados foi divulgada'. O núcleo do sujeito é 'lista', singular; 'candidatos' está dentro de uma expressão ligada a lista e não faz o verbo ir ao plural.",
                "'Os resultados da primeira etapa foram publicados.' Núcleo: resultados → plural → foram publicados.",
                "Contraexemplo: concordar o verbo com a palavra mais próxima. Em 'A quantidade de erros diminuiu', 'erros' é plural, mas o núcleo 'quantidade' é singular.",
                "Erro comum: não localizar o núcleo do sujeito antes de escolher a forma verbal.",
                new String[]{"p_sujeito", "p_verbo", "p_numero"},
                q("Complete: 'A maioria dos candidatos ___ cedo.'", 0,
                        "chegou", "Correto para a leitura em que 'maioria' é o núcleo singular; construções coletivas admitem nuances, mas esta é a concordância básica esperada.", null,
                        "chegaram sempre, obrigatoriamente", "A presença de 'candidatos' plural não torna o plural obrigatório; o núcleo é 'maioria'.", "p_sujeito",
                        "chegaste", "Essa forma é de 2ª pessoa singular e não combina com o sujeito.", "p_verbo",
                        "chegamos", "Essa forma é de 1ª pessoa plural.", "p_verbo"),
                q("Em 'A sequência de mudanças surpreendeu os leitores', por que 'surpreendeu' está no singular?", 3,
                        "Porque 'mudanças' é objeto", "A explicação central é o núcleo do sujeito, não a função isolada de mudanças.", "p_sujeito",
                        "Porque todo verbo depois de 'de' fica singular", "Não existe essa regra.", null,
                        "Porque leitores está no plural", "'leitores' não controla a concordância desse verbo.", "p_sujeito",
                        "Porque o núcleo do sujeito é 'sequência', singular", "Correto.", null)));

        add(m, n(
                "p_conectivos", "Português", "Conectivos e relações de sentido", 2,
                "Conectivos ligam ideias e mostram relações como causa, consequência, oposição, conclusão, condição e adição.",
                "Não decore só a palavra: pergunte QUAL RELAÇÃO ela cria.",
                "causa = porque, já que\nconsequência = portanto, por isso\noposição = mas, porém\ncondição = se, caso\nconcessão = embora, ainda que",
                "O mesmo conteúdo muda quando o conectivo muda. 'Estudou, portanto melhorou' apresenta a melhora como conclusão/consequência. 'Estudou, mas não melhorou' cria contraste entre expectativa e resultado.",
                "'Embora estivesse cansada, continuou estudando.' 'Embora' introduz uma informação que poderia impedir o fato principal, mas não impediu: relação de concessão.",
                "Contraexemplo: trocar 'porque' por 'portanto' sem inverter a lógica. 'Faltou porque estava doente' não equivale a 'Faltou, portanto estava doente'.",
                "Erro comum: escolher conectivo pelo som familiar e ignorar a relação entre as duas partes.",
                new String[]{"p_palavra_frase"},
                q("Qual conectivo mantém a relação de oposição em 'O texto é curto, ___ exige atenção'?", 2,
                        "porque", "Introduziria causa, não contraste.", null,
                        "portanto", "Introduziria conclusão/consequência.", null,
                        "mas", "Correto: contrapõe 'curto' à expectativa de ser fácil.", null,
                        "se", "Criaria condição.", null),
                q("Em 'Como o prazo terminou, o sistema bloqueou novas inscrições', 'como' introduz principalmente:", 0,
                        "causa", "Correto: o término do prazo explica o bloqueio.", null,
                        "oposição", "Não há contraste entre as ideias.", null,
                        "condição", "O prazo já é apresentado como fato, não hipótese.", null,
                        "adição", "A segunda oração não apenas acrescenta um fato: há relação causal.", null)));

        add(m, n(
                "p_info_explicita", "Português", "Informação explícita", 1,
                "Informação explícita é aquela que o texto declara diretamente. A tarefa é localizar e relacionar trechos sem inventar informação nova.",
                "EXPLÍCITO = está dito no texto.",
                "localizar = encontrar onde a informação aparece\nparáfrase = mesma ideia escrita com outras palavras\nreferente = termo ao qual outra expressão aponta",
                "Questões não precisam repetir exatamente as palavras do texto. Se o texto diz 'a prova foi adiada por causa da chuva', uma alternativa pode dizer 'o mau tempo alterou a data'. Você precisa reconhecer a paráfrase sem acrescentar fatos.",
                "Texto: 'O laboratório abrirá às 8h, mas o atendimento ao público começa às 9h.' Pergunta: quando começa o atendimento? Resposta explícita: 9h. Não confunda com horário de abertura do laboratório.",
                "Contraexemplo: concluir que haverá fila porque o atendimento começa às 9h. Isso pode ser possível, mas não foi dito.",
                "Erro comum: responder com conhecimento de mundo em vez de com a informação do texto.",
                new String[]{"p_palavra_frase", "p_pronome"},
                q("Texto: 'As inscrições terminam na sexta; os documentos podem ser enviados até as 18h.' O que está explicitamente informado?", 1,
                        "Quem enviar às 18h será aprovado", "A aprovação não é mencionada.", null,
                        "O envio de documentos é aceito até as 18h", "Correto: essa informação aparece diretamente.", null,
                        "Haverá prorrogação", "O texto não afirma isso.", null,
                        "Sexta-feira será feriado", "Não aparece no texto.", null),
                q("Texto: 'O museu fechou temporariamente para manutenção e reabrirá em novembro.' Qual paráfrase preserva a informação explícita?", 2,
                        "O museu encerrou definitivamente as atividades", "Contradiz 'reabrirá'.", null,
                        "A manutenção começará em novembro", "Novembro é apresentado como momento de reabertura.", null,
                        "O fechamento é provisório e há previsão de reabertura em novembro", "Correto: reformula sem acrescentar informação incompatível.", null,
                        "O museu foi fechado por falta de visitantes", "A causa não foi indicada.", null)));

        add(m, n(
                "p_inferencia", "Português", "Inferência: concluir usando pistas", 2,
                "Inferir é chegar a uma conclusão que não está escrita literalmente, mas é sustentada por pistas do texto.",
                "INFERÊNCIA = pista + relação lógica; não é chute.",
                "implícito = não declarado diretamente\npista textual = detalhe que sustenta a conclusão\ninferência válida = conclusão compatível com as pistas e sem salto exagerado",
                "Uma boa inferência usa o mínimo necessário. Se alguém entra sacudindo o guarda-chuva molhado e o chão está cheio de pegadas úmidas, é razoável inferir chuva recente. Não é razoável inferir que a pessoa odeia chuva: falta pista sobre sentimento.",
                "Texto: 'Ao abrir a conta de luz, Marcelo releu o valor três vezes e imediatamente desligou o ar-condicionado.' É possível inferir que o valor chamou sua atenção e influenciou sua decisão de economizar energia.",
                "Contraexemplo: transformar possibilidade em certeza sem apoio. O texto pode permitir 'provavelmente', mas não 'com certeza' sobre detalhes não fornecidos.",
                "Erro comum: confundir inferência com opinião pessoal.",
                new String[]{"p_info_explicita"},
                q("Texto: 'Lia chegou encharcada e colocou o guarda-chuva aberto perto da porta.' Qual inferência é mais segura?", 0,
                        "Ela esteve recentemente em contato com chuva ou muita água", "Correto: as pistas sustentam essa conclusão sem inventar motivo extra.", null,
                        "Ela esqueceu o guarda-chuva em casa", "Contradiz a presença do guarda-chuva.", null,
                        "Ela gosta de dias chuvosos", "O texto não oferece pista sobre preferência.", null,
                        "Ela caminhou exatamente 2 km", "Não há informação de distância.", null),
                q("Texto: 'O diretor terminou a fala e, por alguns segundos, ninguém aplaudiu. Em seguida, surgiram palmas isoladas.' Qual leitura é melhor sustentada?", 1,
                        "O discurso foi unanimemente aprovado", "O atraso e as palmas isoladas não sustentam unanimidade.", null,
                        "A reação inicial do público foi hesitante ou pouco entusiasmada", "Correto: a sequência de silêncio e palmas isoladas sustenta essa interpretação.", null,
                        "Todos discordaram do discurso", "Silêncio não prova discordância total.", null,
                        "O público não ouviu a fala", "Nada indica problema de audição.", null)));

        add(m, n(
                "p_tese_argumento", "Português", "Tese, argumento e evidência", 3,
                "Tese é a ideia central que um texto argumentativo procura defender. Argumentos são razões usadas para sustentá-la; evidências são dados, exemplos, fatos ou fontes que fortalecem essas razões.",
                "TESE = o que defende.\nARGUMENTO = por que defende.\nEVIDÊNCIA = o que sustenta o argumento.",
                "posição = ponto de vista defendido\nargumento = razão articulada à tese\nevidência = informação verificável ou exemplo relevante\ncontra-argumento = objeção considerada pelo texto",
                "Uma frase temática não é automaticamente tese. Em um texto sobre transporte público, 'As cidades cresceram muito' pode ser contexto. A tese seria algo como 'investir em transporte coletivo é essencial para reduzir congestionamentos', se o restante do texto defender isso.",
                "Miniargumento: 'A escola deve ampliar a biblioteca (tese), porque o número de empréstimos dobrou e há filas para os exemplares mais usados (argumento apoiado por evidências).'
",
                "Contraexemplo: usar uma informação que parece impressionante, mas não se relaciona à tese. Evidência irrelevante não sustenta o raciocínio.",
                "Erro comum: confundir assunto ('bibliotecas') com tese ('a escola deve ampliar a biblioteca').",
                new String[]{"p_info_explicita", "p_inferencia", "p_conectivos"},
                q("Qual frase funciona melhor como tese?", 3,
                        "O trânsito existe em grandes cidades.", "É uma afirmação geral e pouco argumentativa.", null,
                        "Muitas pessoas usam ônibus diariamente.", "Pode ser dado/contexto, não necessariamente uma posição defendida.", null,
                        "Ontem houve congestionamento na avenida central.", "É um fato pontual.", null,
                        "Priorizar corredores de ônibus pode melhorar a mobilidade urbana e deve integrar o planejamento municipal.", "Correto: apresenta uma posição que pode ser defendida por argumentos.", null),
                q("Um texto defende que a escola deve plantar mais árvores. Qual informação seria a evidência mais diretamente relevante?", 2,
                        "A cor preferida dos estudantes é azul", "Não sustenta a relação entre árvores e ambiente escolar.", null,
                        "A escola foi fundada há 40 anos", "Pode ser histórico, mas não sustenta a tese sobre árvores.", null,
                        "Medições mostram temperaturas mais altas nos pátios sem sombra durante o recreio", "Correto: fornece dado ligado ao efeito que a arborização poderia enfrentar.", null,
                        "Há três portões na escola", "Não se relaciona à tese.", null)));

        // ========================= MATEMÁTICA =========================
        add(m, n(
                "m_operacoes", "Matemática", "Operações e ordem de cálculo", 1,
                "Somar, subtrair, multiplicar e dividir são operações básicas. Em uma expressão, a ordem evita resultados diferentes para o mesmo cálculo.",
                "1º parênteses.\n2º potências/raízes.\n3º × e ÷.\n4º + e −.",
                "parênteses = agrupam o que deve ser tratado primeiro\ntermo = parte separada por + ou −\nfator = parte de uma multiplicação\nquociente = resultado de uma divisão",
                "Multiplicação não vem 'sempre antes' de divisão; elas têm a mesma prioridade e são resolvidas da esquerda para a direita. O mesmo vale para soma/subtração. Ex.: 24 ÷ 6 × 2 = 4 × 2 = 8.",
                "Calcule 7 + 3 × (10 − 6). Primeiro: 10−6=4. Depois: 3×4=12. Por fim: 7+12=19.",
                "Contraexemplo: 7+3×4 = 10×4. Isso soma antes da multiplicação sem justificativa.",
                "Erro comum: tentar fazer tudo numa linha e perder a prioridade. Reescreva uma etapa por linha.",
                null,
                q("Quanto vale 18 − 2 × 5?", 1,
                        "80", "Isso trata '18−2' como grupo, mas não há parênteses.", null,
                        "8", "Correto: primeiro 2×5=10; depois 18−10=8.", null,
                        "16", "Subtraiu 2, mas ignorou a multiplicação por 5.", null,
                        "50", "Mistura operações sem respeitar a expressão.", null),
                q("Calcule 36 ÷ 3 × 2 + 4.", 2,
                        "10", "Isso sugere agrupar 3×2 antes, mas divisão e multiplicação têm mesma prioridade e seguem da esquerda para a direita.", null,
                        "20", "Após 36÷3×2, ainda é preciso somar 4.", null,
                        "28", "Correto: 36÷3=12; 12×2=24; 24+4=28.", null,
                        "40", "Não corresponde à ordem operacional correta.", null)));

        add(m, n(
                "m_inteiros", "Matemática", "Números inteiros e sinais", 1,
                "Inteiros incluem negativos, zero e positivos. O sinal indica posição em relação ao zero e muda a forma de somar, subtrair, multiplicar e dividir.",
                "Na reta: mais à direita = maior.\nSinais iguais na multiplicação/divisão → positivo.\nSinais diferentes → negativo.",
                "oposto = mesmo tamanho, sinal contrário: +5 e −5\nvalor absoluto = distância até zero: |−5|=5\nsubtrair = somar o oposto: 7−(−3)=7+3",
                "Para soma com sinais diferentes, compare os valores absolutos e mantenha o sinal do número de maior módulo. Ex.: −12+5 = −7 porque 12 é maior que 5 em distância ao zero.",
                "Temperatura passa de −3 °C para 4 °C. Variação = 4−(−3)=7 °C. O segundo sinal negativo vem da operação de subtrair um número negativo.",
                "Contraexemplo: 'menos com menos dá mais' em qualquer situação. Essa frase vale para multiplicação/divisão de dois negativos e para subtrair um negativo por transformação, não para −4 + −3, que vale −7.",
                "Erro comum: decorar regras sem distinguir soma de multiplicação.",
                new String[]{"m_operacoes"},
                q("Quanto vale −8 + 3?", 1,
                        "11", "Somou os módulos e ignorou os sinais.", null,
                        "−5", "Correto: os sinais são diferentes; 8−3=5 e o maior módulo era negativo.", null,
                        "5", "O módulo está certo, mas o sinal deve acompanhar o número de maior módulo, −8.", null,
                        "−11", "Isso seria −8 + (−3), não −8+3.", null),
                q("Uma conta bancária está em −R$35 e recebe R$50. Qual é o novo saldo?", 2,
                        "−R$85", "Somou os módulos como se os dois valores fossem débitos.", null,
                        "R$85", "Ignora que o saldo inicial era negativo.", null,
                        "R$15", "Correto: −35+50=15.", null,
                        "−R$15", "O crédito de 50 supera a dívida de 35, então o resultado fica positivo.", null)));

        add(m, n(
                "m_fracao", "Matemática", "Frações: parte, razão e número", 1,
                "Uma fração a/b representa a divisão a÷b, com b diferente de zero. Ela também pode representar parte de um todo, razão entre quantidades ou posição na reta numérica.",
                "NUMERADOR = quantas partes.\nDENOMINADOR = em quantas partes iguais a unidade foi dividida.",
                "numerador = número de cima\ndenominador = número de baixo\nequivalentes = frações com o mesmo valor, como 1/2 e 3/6\nsimplificar = dividir numerador e denominador pelo mesmo fator não nulo",
                "O denominador não diz 'quantas partes eu tenho', e sim o tamanho relativo de cada parte ao dividir a unidade em partes iguais. Frações equivalentes ocupam o mesmo ponto na reta. Multiplicar numerador e denominador pelo mesmo número mantém o valor.",
                "3/4 de 20: dividir 20 em 4 partes iguais dá 5 por parte; tomar 3 partes dá 15. Outra forma: 20×3/4 = 15.",
                "Contraexemplo: dizer que 1/8 é maior que 1/6 porque 8>6. Com mesmo numerador 1, mais divisões criam partes menores; 1/8<1/6.",
                "Erro comum: comparar numerador e denominador separadamente em vez do valor da fração.",
                new String[]{"m_operacoes"},
                q("Qual fração é equivalente a 3/5?", 2,
                        "6/5", "Dobrou apenas o numerador; o valor mudou.", null,
                        "3/10", "Dobrou apenas o denominador; o valor caiu pela metade.", null,
                        "9/15", "Correto: multiplicou numerador e denominador por 3.", null,
                        "8/10", "8/10=4/5, não 3/5.", null),
                q("Uma turma concluiu 18 de 24 tarefas. Qual fração simplificada representa a parte concluída?", 1,
                        "18/6", "O denominador deveria representar o total de tarefas, 24, antes da simplificação.", null,
                        "3/4", "Correto: 18/24 ÷6 = 3/4.", null,
                        "4/3", "Inverte a razão e produz valor maior que 1, incompatível com parte do total.", null,
                        "6/24", "Isso representa 1/4, não 18 de 24.", null)));

        add(m, n(
                "m_fracao_operacoes", "Matemática", "Operações com frações", 1,
                "Para somar ou subtrair frações, use denominadores iguais. Para multiplicar, multiplique numeradores e denominadores. Para dividir, multiplique pela fração inversa da segunda.",
                "+ ou − → mesmo denominador.\n× → cima×cima, baixo×baixo.\n÷ → multiplica pelo inverso da segunda.",
                "MMC = menor múltiplo comum, útil para denominador comum\ninverso de a/b = b/a, se a≠0\nsimplificação = reduzir a forma sem mudar o valor",
                "Você não pode somar 1/2 + 1/3 como 2/5 porque as partes têm tamanhos diferentes. Transforme em sextos: 3/6+2/6=5/6.",
                "2/3 ÷ 4/5 = 2/3 × 5/4 = 10/12 = 5/6. Dividir por 4/5 pergunta quantos grupos de tamanho 4/5 cabem em 2/3; a regra do inverso preserva essa relação.",
                "Contraexemplo: na multiplicação, procurar MMC sem necessidade. 2/3×5/7 pode ser multiplicado diretamente.",
                "Erro comum: aplicar a regra de uma operação em outra.",
                new String[]{"m_fracao", "m_operacoes"},
                q("Quanto vale 1/4 + 1/6?", 0,
                        "5/12", "Correto: MMC(4,6)=12; 1/4=3/12 e 1/6=2/12; soma=5/12.", null,
                        "2/10", "Somou numeradores e denominadores, o que não preserva o tamanho das partes.", "m_fracao",
                        "1/10", "Não corresponde à soma das duas frações positivas.", null,
                        "2/24", "Multiplicou numeradores e denominadores como se fosse multiplicação.", null),
                q("Uma receita usa 3/4 de xícara por porção. Para fazer 2/3 de uma porção, quanto usar?", 2,
                        "17/12", "Isso resulta de uma soma, mas a situação pede uma fração de outra quantidade.", null,
                        "9/8", "Maior que 3/4, embora estejamos fazendo menos que uma porção.", null,
                        "1/2", "Correto: 2/3 de 3/4 = 2/3×3/4=6/12=1/2.", null,
                        "2/7", "Mistura numeradores e denominadores sem operação válida.", null)));

        add(m, n(
                "m_decimal", "Matemática", "Números decimais", 1,
                "Decimal é outra forma de representar números racionais usando potências de 10. A posição dos algarismos determina décimos, centésimos, milésimos etc.",
                "0,1 = 1 décimo.\n0,01 = 1 centésimo.\nAlinhe a vírgula para + e −.",
                "parte inteira = antes da vírgula\ndécimo = primeira casa\ncentésimo = segunda casa\nvalor posicional = valor depende da posição",
                "0,5 = 5/10 = 1/2. 0,50 tem o mesmo valor, pois o zero final não altera a quantidade. Já 0,05 é cinco centésimos, dez vezes menor que 0,5.",
                "12,7 + 3,45: alinhe as casas: 12,70 + 3,45 = 16,15.",
                "Contraexemplo: 0,9 < 0,10 porque 9<10. Na verdade 0,9=0,90 e 0,90>0,10.",
                "Erro comum: comparar os algarismos sem alinhar as casas decimais.",
                new String[]{"m_fracao", "m_operacoes"},
                q("Qual número é maior?", 1,
                        "0,48", "0,48 = 48 centésimos.", null,
                        "0,5", "Correto: 0,5=0,50, e 50 centésimos > 48 centésimos.", null,
                        "São iguais", "0,50 e 0,48 não são iguais.", null,
                        "Não é possível comparar", "Basta escrever com o mesmo número de casas.", null),
                q("Um produto custa R$ 18,75 e aumenta R$ 2,6. Qual o novo preço?", 2,
                        "R$ 18,101", "Concatenou casas decimais em vez de somar valores posicionais.", null,
                        "R$ 20,35", "Isso corresponderia a somar 1,60, não 2,60.", null,
                        "R$ 21,35", "Correto: 18,75 + 2,60 = 21,35.", null,
                        "R$ 44,75", "Não corresponde à operação pedida.", null)));

        add(m, n(
                "m_razao", "Matemática", "Razão e proporção", 1,
                "Razão compara duas grandezas por divisão. Proporção afirma que duas razões são iguais.",
                "RAZÃO a:b = a/b.\nPROPORÇÃO = duas razões iguais.",
                "termos da proporção = a/b = c/d\nproduto dos meios/extremos = em a/b=c/d, vale a·d=b·c\ngrandezas diretamente proporcionais = crescem/decrescem mantendo razão constante",
                "Se 2 cadernos custam R$14 no mesmo preço unitário, a razão preço/quantidade é 14/2=7 reais por caderno. Para 5 cadernos: 5×7=35.",
                "Mapa em escala 1:50 000: 1 cm no mapa representa 50 000 cm reais = 500 m. A escala é uma razão entre medida no desenho e medida real na mesma unidade.",
                "Contraexemplo: toda relação crescente é proporcional. Se uma tarifa tem taxa fixa + consumo, dobrar o consumo não necessariamente dobra o total.",
                "Erro comum: montar a proporção sem verificar quais grandezas correspondem entre si.",
                new String[]{"m_fracao", "m_decimal"},
                q("Se 3 kg custam R$ 24 no mesmo preço por kg, quanto custam 5 kg?", 2,
                        "R$ 29", "Somou 5 ao preço sem usar a razão por kg.", null,
                        "R$ 32", "Preço unitário é 24/3=8; 4 kg custariam 32.", null,
                        "R$ 40", "Correto: 24/3=8 reais/kg; 5×8=40.", null,
                        "R$ 120", "Multiplicou 24×5 sem dividir pelo número de kg original.", null),
                q("Uma máquina produz 120 peças em 3 h a taxa constante. Quanto produz em 7,5 h?", 1,
                        "300", "Isso seria 7,5×40=300? Na verdade 120/3=40; 40×7,5=300. Portanto esta opção seria correta se fosse 300.", null,
                        "300 peças", "Correto: taxa = 40 peças/h; 40×7,5=300.", null,
                        "270 peças", "Equivale a 36 peças/h, diferente da taxa dada.", null,
                        "900 peças", "Multiplicou 120×7,5 sem dividir pelo tempo original.", null)));

        // Corrige duplicidade textual propositalmente mantendo apenas uma alternativa correta na questão acima.
        // O item 0 é alterado em create() logo após a criação do nó.
        Node razaoNode = m.get("m_razao");
        // Java arrays são mutáveis; substituímos o rótulo da primeira alternativa por um distrator real.
        razaoNode.transfer.options[0] = "240 peças";
        razaoNode.transfer.feedback[0] = "240 peças corresponderiam a apenas 6 horas nessa taxa.";

        add(m, n(
                "m_porcentagem", "Matemática", "Porcentagem", 1,
                "Porcentagem é uma razão com denominador 100. 15% significa 15/100 = 0,15.",
                "x% = x/100.\nAUMENTO: valor + parte.\nDESCONTO: valor − parte.",
                "percentual = taxa por 100\nfator de aumento = 1 + taxa decimal\nfator de desconto = 1 − taxa decimal\nvariação percentual = diferença ÷ valor inicial ×100%",
                "20% de 250 = 0,20×250=50. Desconto de 20%: 250−50=200. Também dá para usar fator 0,80×250=200.",
                "Um preço sobe de 80 para 100: aumento absoluto 20. Percentual = 20/80=0,25=25%. Dividir por 100 daria erro porque a referência é o valor inicial, 80.",
                "Contraexemplo: subir 20% e depois cair 20% volta ao valor original. Não volta: 100→120→96, pois a segunda porcentagem usa nova base.",
                "Erro comum: tratar 15% como 15 reais independentemente do valor base.",
                new String[]{"m_decimal", "m_razao"},
                q("Uma taxa de R$ 240 recebe desconto de 15%. Qual o valor final?", 0,
                        "R$ 204", "Correto: 15% de 240=36; 240−36=204.", null,
                        "R$ 225", "Subtraiu 15 reais, confundindo porcentagem com valor absoluto.", "m_decimal",
                        "R$ 216", "Isso representa desconto de 10%.", null,
                        "R$ 180", "Isso representa desconto de 25%.", null),
                q("Um produto aumenta 10% e depois recebe desconto de 10%. Partindo de R$200, qual o preço final?", 1,
                        "R$200", "Percentuais sucessivos usam bases diferentes; eles não se anulam automaticamente.", null,
                        "R$198", "Correto: 200×1,10=220; 220×0,90=198.", null,
                        "R$180", "Aplicou apenas o desconto ao valor inicial.", null,
                        "R$220", "Aplicou apenas o aumento.", null)));

        add(m, n(
                "m_distributiva", "Matemática", "Propriedade distributiva", 1,
                "Distributiva liga multiplicação a soma/subtração: a(b+c)=ab+ac. É uma das pontes principais entre aritmética e álgebra.",
                "Número fora do parêntese multiplica TODOS os termos dentro.",
                "fator = elemento que multiplica\ntermo = parcela separada por + ou −\nexpandir = retirar parênteses aplicando distributiva\ncolocar em evidência = fazer o caminho inverso",
                "3(x+4)=3x+12. Em −2(x−5), o fator −2 multiplica x e também −5: −2x+10.",
                "2(3x−4)+5 = 6x−8+5 = 6x−3. Primeiro distribua, depois reduza termos semelhantes.",
                "Contraexemplo: 4(x+2)=4x+2. O 2 também precisa ser multiplicado por 4.",
                "Erro comum: perder o sinal quando o fator externo é negativo.",
                new String[]{"m_operacoes", "m_inteiros"},
                q("Qual é a expansão correta de −3(x−4)?", 2,
                        "−3x−4", "O −3 não foi distribuído ao segundo termo.", null,
                        "−3x−12", "−3×(−4)=+12, não −12.", "m_inteiros",
                        "−3x+12", "Correto: −3×x + (−3)×(−4).", null,
                        "3x−12", "O sinal do termo com x foi trocado.", "m_inteiros"),
                q("Simplifique 5(2x−3)−2(x+4).", 3,
                        "8x−7", "Os termos constantes foram combinados incorretamente.", null,
                        "12x−23", "O segundo grupo deve ser subtraído: −2x−8.", "m_distributiva",
                        "8x−15", "Faltou considerar −2×4=−8.", null,
                        "8x−23", "Correto: 10x−15−2x−8=8x−23.", null)));

        add(m, n(
                "m_algebra", "Matemática", "Expressões algébricas e termos semelhantes", 1,
                "Álgebra usa letras para representar números desconhecidos ou variáveis. Termos semelhantes têm a mesma parte literal e podem ser reduzidos.",
                "3x + 5x = 8x.\n3x + 5 NÃO vira 8x.",
                "variável = letra que pode assumir valores\ncoeficiente = número que multiplica a variável\ntermo semelhante = mesma parte literal e mesmos expoentes\nexpressão = combinação de números, letras e operações",
                "2x+3x−4 = 5x−4. Já 2x+3x² não pode ser reduzido para 5x³: x e x² representam partes literais diferentes.",
                "Se x=4, a expressão 3x−2 vale 3·4−2=10. Substituir valor é diferente de resolver uma equação: aqui apenas avaliamos uma expressão.",
                "Contraexemplo: 2x+3=5x. O 3 não tem x, então não é termo semelhante a 2x.",
                "Erro comum: somar coeficientes de termos que não são semelhantes.",
                new String[]{"m_distributiva", "m_inteiros"},
                q("Simplifique 4x−2+3x+5.", 1,
                        "7x+7", "−2+5=3, não 7.", null,
                        "7x+3", "Correto: 4x+3x=7x e −2+5=3.", null,
                        "12x", "Mistura constantes com termos em x.", null,
                        "7x−7", "O cálculo das constantes ficou com sinal errado.", "m_inteiros"),
                q("Para x=−2, quanto vale x²+3x−1?", 0,
                        "−3", "Correto: (−2)² +3(−2)−1 =4−6−1=−3.", null,
                        "−11", "Provavelmente tratou (−2)² como −4; o quadrado de −2 é +4.", "m_inteiros",
                        "1", "Não considera corretamente o termo 3x.", null,
                        "9", "Mistura sinais ou operações na substituição.", "m_inteiros")));

        add(m, n(
                "m_equacao1", "Matemática", "Equação do 1º grau", 2,
                "Equação é uma igualdade com incógnita. No 1º grau, a maior potência da incógnita é 1. Resolver é encontrar valor que torna a igualdade verdadeira.",
                "Objetivo: deixar x sozinho fazendo operações equivalentes nos dois lados.",
                "incógnita = valor desconhecido\nmembro = cada lado da igualdade\nsolução = valor que torna a igualdade verdadeira\nequivalência = transformação que preserva as soluções",
                "Em vez de decorar 'passa para o outro lado trocando o sinal', pense em equilíbrio. 2x+3=13. Subtraia 3 dos dois lados: 2x=10. Divida os dois lados por 2: x=5.",
                "x−5=12. Some 5 nos dois lados: x−5+5=12+5 → x=17. O '+5' aparece porque queremos cancelar o −5 mantendo a igualdade equilibrada.",
                "Contraexemplo: alterar só um lado da equação. Se 3x=12 e você subtrai 3 apenas do lado esquerdo, cria outra igualdade que não é equivalente.",
                "Erro comum: trocar sinais mecanicamente e não conseguir explicar por que a transformação funciona.",
                new String[]{"m_algebra", "m_distributiva", "m_inteiros"},
                q("Resolva x−5=12.", 2,
                        "x=7", "Isso subtrai 5 do lado direito; para cancelar −5 do lado esquerdo, somamos 5 aos dois lados.", null,
                        "x=12", "Ignora o −5 que acompanha x.", null,
                        "x=17", "Correto: x−5+5=12+5, então x=17.", null,
                        "x=−17", "O sinal não decorre da operação equivalente correta.", "m_inteiros"),
                q("Resolva 3(x−2)+4=2x+9.", 1,
                        "x=1", "Se x=1, lado esquerdo=1 e direito=11; não satisfaz a igualdade.", null,
                        "x=11", "Correto: 3x−6+4=2x+9 → 3x−2=2x+9 → x=11.", null,
                        "x=7", "Surge se a distributiva ou a transposição dos termos for feita incorretamente.", "m_distributiva",
                        "x=−11", "Os sinais foram alterados indevidamente.", "m_inteiros")));

        add(m, n(
                "m_sistema1", "Matemática", "Sistema de equações do 1º grau", 2,
                "Sistema reúne duas ou mais equações que devem ser verdadeiras ao mesmo tempo. A solução é o conjunto de valores que satisfaz todas.",
                "Uma equação dá uma condição. O sistema exige cumprir TODAS.",
                "substituição = isolar uma variável e colocar a expressão na outra equação\neliminação = somar/subtrair equações para cancelar uma variável\npar ordenado = solução (x,y)",
                "x+y=10 e x−y=2. Somando as equações: 2x=12 → x=6. Depois y=4. Verifique nas duas: 6+4=10 e 6−4=2.",
                "Problema: ingressos inteiros custam 20 e meia 10. Foram 30 ingressos e arrecadação 480. x+y=30; 20x+10y=480. Resolver o sistema traduz o contexto.",
                "Contraexemplo: resolver apenas a primeira equação e escolher qualquer par que sirva nela. A solução precisa servir em todas.",
                "Erro comum: montar equações sem definir o que cada variável representa.",
                new String[]{"m_equacao1"},
                q("No sistema x+y=9 e x−y=3, qual é a solução?", 0,
                        "(6,3)", "Correto: 6+3=9 e 6−3=3.", null,
                        "(3,6)", "Satisfaz a soma, mas 3−6=−3.", null,
                        "(9,3)", "A soma seria 12.", null,
                        "(6,6)", "A soma seria 12 e a diferença 0.", null),
                q("Dois números somam 26 e o maior excede o menor em 8. Quais são?", 2,
                        "10 e 16", "Somam 26, mas a diferença é 6.", null,
                        "8 e 18", "Somam 26, mas a diferença é 10.", null,
                        "9 e 17", "Correto: 9+17=26 e 17−9=8.", null,
                        "13 e 13", "A diferença é zero.", null)));

        add(m, n(
                "m_fatoracao", "Matemática", "Produtos notáveis e fatoração", 2,
                "Fatorar é escrever uma expressão como multiplicação. Produtos notáveis são padrões de multiplicação que aparecem com frequência e ajudam a expandir ou fatorar.",
                "(a+b)²=a²+2ab+b².\n(a−b)²=a²−2ab+b².\na²−b²=(a−b)(a+b).",
                "fator comum = termo que divide todas as parcelas\ndiferença de quadrados = a²−b²\ntrinômio quadrado perfeito = resultado de quadrado de binômio",
                "6x+12 = 6(x+2). O fator 6 foi colocado em evidência. Já x²−9 = x²−3²=(x−3)(x+3).",
                "x²+6x+9 = x²+2·3·x+3²=(x+3)². Reconhecer o padrão evita tentativas aleatórias.",
                "Contraexemplo: (a+b)²=a²+b². Falta o termo 2ab.",
                "Erro comum: usar um padrão sem conferir todos os termos e sinais.",
                new String[]{"m_distributiva", "m_algebra"},
                q("Qual é a expansão de (x+4)²?", 2,
                        "x²+16", "Falta o termo do meio 2·x·4.", null,
                        "x²+4x+16", "O termo do meio deveria ser 8x.", null,
                        "x²+8x+16", "Correto.", null,
                        "x²−8x+16", "Esse seria o padrão de (x−4)².", null),
                q("Fatore x²−25.", 1,
                        "(x−5)²", "Isso expandiria para x²−10x+25.", null,
                        "(x−5)(x+5)", "Correto: diferença de quadrados x²−5².", null,
                        "x(x−25)", "Expandiria para x²−25x.", null,
                        "(x−25)(x+1)", "Não produz x²−25.", null)));

        add(m, n(
                "m_equacao2", "Matemática", "Equação do 2º grau", 3,
                "Equação do 2º grau pode ser escrita como ax²+bx+c=0, com a≠0. Resolver é encontrar os valores de x que tornam a expressão zero.",
                "1º organize ax²+bx+c=0.\n2º tente fatorar.\n3º se necessário, use Δ=b²−4ac e Bhaskara.",
                "coeficientes = a,b,c\ndiscriminante Δ = b²−4ac\nraízes = soluções da equação\nBhaskara = x=(−b±√Δ)/(2a)",
                "x²−5x+6=0. Procure dois números que multiplicam 6 e somam −5: −2 e −3. Então (x−2)(x−3)=0 → x=2 ou x=3.",
                "2x²−3x−2=0. a=2,b=−3,c=−2. Δ=9+16=25. x=(3±5)/4 → x=2 ou x=−1/2.",
                "Contraexemplo: chamar 3x+2=0 de 2º grau só porque há três símbolos. O grau depende do maior expoente da incógnita.",
                "Erro comum: usar b sem sinal. Se b=−3, então −b=+3 e b²=(−3)²=9.",
                new String[]{"m_equacao1", "m_fatoracao", "m_algebra"},
                q("Quais são as raízes de x²−7x+12=0?", 3,
                        "1 e 12", "Multiplicam 12, mas somam 13, não 7.", null,
                        "−3 e −4", "Produziriam soma −7 das raízes em outra configuração; teste na equação.", "m_inteiros",
                        "2 e 6", "Multiplicam 12, mas somam 8.", null,
                        "3 e 4", "Correto: (x−3)(x−4)=0.", null),
                q("Para x²+4x+8=0, o discriminante é negativo. O que isso indica no conjunto dos números reais?", 1,
                        "Há duas raízes reais iguais", "Isso ocorre quando Δ=0.", null,
                        "Não há raízes reais", "Correto: Δ<0 impede √Δ real.", null,
                        "Há duas raízes reais diferentes", "Isso ocorre quando Δ>0.", null,
                        "A equação vira de 1º grau", "O termo x² continua presente.", "m_equacao1")));

        add(m, n(
                "m_funcao", "Matemática", "Função: entrada, regra e saída", 3,
                "Função associa cada valor permitido de entrada a exatamente uma saída. Domínio é o conjunto de entradas; imagem é o conjunto de saídas realmente obtidas; contradomínio é o conjunto declarado como destino possível.",
                "DOMÍNIO = entra.\nREGRA = transforma.\nIMAGEM = sai de verdade.",
                "domínio = entradas permitidas\ncontradomínio = conjunto de chegada definido\nimagem = valores efetivamente produzidos\nlei de formação = regra que relaciona entrada e saída",
                "Se f(x)=2x e domínio {1,2,3}, então as saídas são {2,4,6}; essa é a imagem. Um contradomínio poderia ser, por exemplo, {0,1,2,3,4,5,6,7}; ele é maior que a imagem.",
                "Uma relação não é função se a MESMA entrada tiver duas saídas diferentes. Entradas diferentes podem ter a mesma saída sem problema.",
                "Contraexemplo: pensar que contradomínio e imagem são sempre iguais. Só são iguais em funções sobrejetoras para o contradomínio escolhido.",
                "Erro comum: confundir os valores que entram com os que saem.",
                new String[]{"m_algebra", "m_equacao1"},
                q("Para f(x)=3x−1 e domínio {0,1,2}, qual é a imagem?", 0,
                        "{−1,2,5}", "Correto: f(0)=−1, f(1)=2, f(2)=5.", null,
                        "{0,1,2}", "Esse é o domínio, não a imagem.", null,
                        "{3,6,9}", "Ignora o −1 da regra.", "m_algebra",
                        "{−1,1,3}", "Os valores foram calculados como 2x−1 ou outra regra, não 3x−1.", "m_algebra"),
                q("Uma relação associa 1→4, 2→5, 2→7 e 3→8. Por que ela não é função de {1,2,3}?", 2,
                        "Porque duas entradas diferentes podem gerar valores diferentes", "Isso é normal em funções.", null,
                        "Porque a saída 8 é maior que a entrada 3", "Tamanho relativo não define função.", null,
                        "Porque a mesma entrada 2 recebeu duas saídas, 5 e 7", "Correto: uma entrada não pode ter duas imagens diferentes.", null,
                        "Porque há três entradas", "Funções podem ter qualquer quantidade de entradas no domínio.", null)));

        add(m, n(
                "m_afim", "Matemática", "Função afim (1º grau)", 3,
                "Função afim tem forma f(x)=ax+b. Seu gráfico é uma reta. O coeficiente a controla a inclinação/taxa de variação; b é o valor quando x=0.",
                "f(x)=ax+b.\na = quanto y muda quando x aumenta 1.\nb = onde a reta corta o eixo y.",
                "coeficiente angular = a\ncoeficiente linear = b\nraiz/zero = x para o qual f(x)=0\ncrescente = a>0; decrescente = a<0",
                "f(x)=2x+3. Se x aumenta 1, f aumenta 2. f(0)=3. A raiz vem de 2x+3=0 → x=−3/2.",
                "Tarifa de táxi modelada por C(x)=6+2,5x: 6 é taxa fixa e 2,5 é custo por quilômetro. Essa interpretação é mais importante que decorar nomes.",
                "Contraexemplo: y=3x²+1 não é afim porque possui x²; o gráfico não é reta.",
                "Erro comum: confundir a raiz (interseção com eixo x) com b (interseção com eixo y).",
                new String[]{"m_funcao", "m_equacao1"},
                q("Na função f(x)=−4x+7, o que significa o coeficiente −4?", 1,
                        "Quando x=0, y=−4", "Quando x=0, y=7; esse é o papel de b.", null,
                        "A cada aumento de 1 em x, f(x) diminui 4", "Correto: é a taxa de variação da reta.", null,
                        "A raiz é sempre −4", "A raiz é obtida resolvendo −4x+7=0.", "m_equacao1",
                        "O gráfico é uma parábola", "Função afim tem gráfico de reta.", null),
                q("Uma reta passa pelos pontos (2,5) e (6,13). Qual é sua taxa de variação a?", 2,
                        "1", "A variação de y é 8 e de x é 4; 8/4=2.", null,
                        "4", "Esse é Δx, não a razão Δy/Δx.", null,
                        "2", "Correto: (13−5)/(6−2)=8/4=2.", null,
                        "8", "Esse é Δy, não a taxa por unidade de x.", null)));

        add(m, n(
                "m_quadratica", "Matemática", "Função quadrática", 3,
                "Função quadrática tem forma f(x)=ax²+bx+c, a≠0. O gráfico é uma parábola. As raízes são os x onde o gráfico cruza o eixo x.",
                "a>0: abre para cima.\na<0: abre para baixo.\nraízes: f(x)=0.",
                "parábola = gráfico da função quadrática\nvértice = ponto de máximo ou mínimo\neixo de simetria = x=−b/(2a)\nraízes = soluções da equação quadrática associada",
                "f(x)=x²−4x+3=(x−1)(x−3). Raízes 1 e 3. Como a=1>0, a parábola abre para cima. Eixo de simetria x=2.",
                "Para f(x)=−x²+6x−5, a<0: há um valor máximo no vértice. Em problemas, o vértice pode representar altura máxima, lucro máximo ou outra grandeza.",
                "Contraexemplo: achar que todo gráfico curvo é quadrático. É preciso verificar a relação algébrica ou propriedades específicas.",
                "Erro comum: estudar a parábola sem ligar gráfico, equação e contexto.",
                new String[]{"m_equacao2", "m_funcao"},
                q("Para f(x)=2x²−8x+6, qual é o eixo de simetria?", 1,
                        "x=−2", "Use x=−b/(2a): −(−8)/(4)=2.", null,
                        "x=2", "Correto.", null,
                        "x=6", "c=6 é intercepto em y, não eixo de simetria.", null,
                        "x=8", "Usa o módulo de b sem dividir por 2a.", "m_equacao2"),
                q("Uma função quadrática tem raízes 2 e 8 e abre para cima. Em qual intervalo seus valores são negativos?", 2,
                        "x<2", "Fora das raízes, uma parábola que abre para cima fica positiva.", null,
                        "x>8", "Também é região externa e positiva.", null,
                        "2<x<8", "Correto: entre as raízes a parábola fica abaixo do eixo x.", null,
                        "Nunca", "Duas raízes distintas e abertura para cima implicam trecho negativo entre elas.", null)));

        add(m, n(
                "m_pitagoras", "Matemática", "Triângulo retângulo e Teorema de Pitágoras", 2,
                "Em triângulo retângulo, o quadrado da hipotenusa é igual à soma dos quadrados dos catetos: a²=b²+c².",
                "HIPOTENUSA = lado oposto ao ângulo de 90° e é o maior lado.",
                "catetos = lados que formam o ângulo reto\nhipotenusa = lado oposto ao ângulo reto\nterno pitagórico = conjunto como 3,4,5 que satisfaz a relação",
                "Se os catetos medem 6 e 8: h²=36+64=100 → h=10. Não some 6+8: comprimento diagonal não é soma direta dos lados.",
                "Para verificar se lados 7,24,25 formam triângulo retângulo: 7²+24²=49+576=625=25².",
                "Contraexemplo: usar Pitágoras em qualquer triângulo sem ângulo de 90°.",
                "Erro comum: escolher como hipotenusa um cateto só porque aparece primeiro no desenho.",
                new String[]{"m_operacoes", "m_algebra"},
                q("Catetos 9 e 12. Qual a hipotenusa?", 3,
                        "21", "Somou os catetos diretamente.", null,
                        "108", "Multiplicou os catetos.", null,
                        "225", "Esse é h², não h.", null,
                        "15", "Correto: h²=81+144=225; h=15.", null),
                q("Uma escada de 13 m encosta numa parede e sua base está 5 m da parede. A que altura chega?", 1,
                        "18 m", "Somou os lados.", null,
                        "12 m", "Correto: 13²=5²+h² → h²=169−25=144 → h=12.", null,
                        "8 m", "Não satisfaz 5²+8²=13².", null,
                        "√194 m", "Somou quadrados em vez de isolar o cateto desconhecido.", null)));

        // ========================= FÍSICA =========================
        add(m, n(
                "f_unidades", "Física", "Grandezas, unidades e conversão", 1,
                "Grandeza é algo que pode ser medido. Unidade é o padrão usado para expressar a medida. Em Física, a conta só faz sentido se as unidades forem compatíveis.",
                "VALOR + UNIDADE.\nAntes da fórmula, padronize as unidades.",
                "comprimento = metro (m)\ntempo = segundo (s)\nmassa = quilograma (kg)\nvelocidade = m/s\nSI = Sistema Internacional de Unidades",
                "1 km=1000 m; 1 h=3600 s. Para transformar 72 km/h em m/s: 72×1000/3600=20 m/s. Regra prática km/h ÷3,6 = m/s.",
                "Uma fórmula v=d/t exige distância e tempo em unidades coerentes. 500 m / 2 min não deve ser escrito como 250 m/s; 2 min=120 s, então v≈4,17 m/s.",
                "Contraexemplo: somar 3 m + 5 s. São grandezas diferentes; a soma não representa uma medida física do mesmo tipo.",
                "Erro comum: substituir números na fórmula antes de converter as unidades.",
                new String[]{"m_razao", "m_decimal"},
                q("90 km/h equivalem a quantos m/s?", 2,
                        "324 m/s", "Multiplicou por 3,6 em vez de dividir.", null,
                        "9 m/s", "Dividiu por 10, não por 3,6.", null,
                        "25 m/s", "Correto: 90/3,6=25.", null,
                        "250 m/s", "Erro de fator 10.", null),
                q("Um móvel percorre 600 m em 2 min. Qual sua velocidade média em m/s?", 1,
                        "300 m/s", "Usou 2 como se fossem segundos.", null,
                        "5 m/s", "Correto: 2 min=120 s; 600/120=5.", null,
                        "12 m/s", "Não corresponde à razão distância/tempo convertida.", null,
                        "0,3 m/s", "Divisão ou conversão incorreta.", null)));

        add(m, n(
                "f_velocidade", "Física", "Velocidade média e movimento", 2,
                "Velocidade média relaciona deslocamento/distância considerada com intervalo de tempo. Em problemas simples de rapidez média, usa-se v=Δs/Δt.",
                "VELOCIDADE MÉDIA = quanto de espaço muda por unidade de tempo.",
                "Δ = variação = final−inicial\ntrajetória = caminho\nposição = localização em um referencial\nmovimento uniforme = velocidade constante em modelo básico",
                "Se um carro percorre 150 km em 3 h, rapidez média=50 km/h. Isso não significa que o velocímetro ficou em 50 o tempo todo: média resume o percurso.",
                "Percurso em duas etapas: 60 km a 60 km/h (1 h) e 60 km a 30 km/h (2 h). Total 120 km em 3 h → média 40 km/h, não a média aritmética 45.",
                "Contraexemplo: calcular média de velocidades sem considerar os tempos/distâncias de cada etapa.",
                "Erro comum: confundir velocidade média do percurso com valor instantâneo.",
                new String[]{"f_unidades", "m_razao"},
                q("Um ciclista percorre 24 km em 1,5 h. Qual a rapidez média?", 0,
                        "16 km/h", "Correto: 24/1,5=16.", null,
                        "36 km/h", "Multiplicou em vez de dividir.", null,
                        "22,5 km/h", "Não corresponde à razão distância/tempo.", null,
                        "0,0625 km/h", "Inverteu a razão.", "m_razao"),
                q("Um ônibus percorre 40 km em 1 h e depois 40 km em 2 h. Qual a rapidez média no total?", 3,
                        "30 km/h", "É média aritmética de 40 e 20, mas os trechos têm durações diferentes.", null,
                        "40 km/h", "Considera apenas o primeiro trecho.", null,
                        "20 km/h", "Considera apenas o segundo trecho.", null,
                        "80/3 km/h (≈26,7 km/h)", "Correto: 80 km em 3 h.", null)));

        add(m, n(
                "f_newton", "Física", "Força resultante e Leis de Newton", 2,
                "Força resultante é a soma vetorial das forças. Pela 2ª Lei de Newton, Fres=m·a. Resultante zero significa aceleração zero, não necessariamente velocidade zero.",
                "ΣF = m·a.\nΣF=0 → velocidade não muda.",
                "inércia = tendência de manter estado de movimento\nresultante = efeito combinado das forças\naceleração = variação da velocidade\nação e reação = forças de mesma intensidade e sentidos opostos em corpos diferentes",
                "Um bloco de 2 kg recebe 10 N para a direita e 4 N para a esquerda. Resultante=6 N direita. a=6/2=3 m/s² para a direita.",
                "Um carro em velocidade constante numa estrada reta pode ter forças atuando; se elas se equilibram, a resultante é zero e não há aceleração.",
                "Contraexemplo: 'se a resultante é zero, o corpo está parado'. Pode estar em movimento retilíneo uniforme.",
                "Erro comum: cancelar ação e reação entre si. Elas atuam em corpos diferentes.",
                new String[]{"f_unidades", "m_inteiros"},
                q("Um corpo de 5 kg sofre resultante de 20 N. Qual a aceleração?", 1,
                        "100 m/s²", "Multiplicou F por m; a=F/m.", null,
                        "4 m/s²", "Correto: 20/5=4.", null,
                        "25 m/s²", "Somou massa e força, grandezas diferentes.", "f_unidades",
                        "0,25 m/s²", "Inverteu a divisão.", null),
                q("Um objeto move-se em linha reta com velocidade constante diferente de zero. O que podemos afirmar sobre a resultante das forças no modelo de Newton?", 2,
                        "É obrigatoriamente para frente", "Se houvesse resultante para frente, haveria aceleração.", null,
                        "É obrigatoriamente para trás", "Também produziria aceleração.", null,
                        "É zero", "Correto: velocidade constante implica aceleração zero; então ΣF=0.", null,
                        "Não existe nenhuma força atuando", "Pode haver várias forças equilibradas.", null)));

        add(m, n(
                "f_calor", "Física", "Temperatura, calor e mudança de estado", 2,
                "Temperatura relaciona-se ao estado térmico; calor é energia transferida devido a diferença de temperatura. Eles não são sinônimos.",
                "TEMPERATURA = estado térmico.\nCALOR = energia em transferência.",
                "equilíbrio térmico = temperaturas se igualam\ncalor sensível = altera temperatura\ncalor latente = associado a mudança de estado sem variar temperatura no modelo ideal\ncondução/convecção/radiação = formas de transferência",
                "Coloque uma colher fria em sopa quente: energia térmica flui da sopa para a colher até se aproximarem do equilíbrio. Não dizemos que a colher 'ganhou temperatura'; ela ganhou energia e sua temperatura aumentou.",
                "Durante a fusão do gelo a 0 °C sob condições usuais, energia pode continuar entrando enquanto a temperatura permanece aproximadamente constante até terminar a mudança de fase.",
                "Contraexemplo: um corpo 'tem calor'. Em Física escolar, calor designa energia em trânsito, não propriedade armazenada.",
                "Erro comum: associar objeto maior a temperatura maior apenas porque possui mais energia interna total.",
                new String[]{"f_unidades"},
                q("Dois corpos a temperaturas diferentes são colocados em contato e isolados. Em que sentido ocorre espontaneamente a transferência de calor?", 0,
                        "Do mais quente para o mais frio", "Correto, até o equilíbrio térmico.", null,
                        "Do mais frio para o mais quente", "Isso não ocorre espontaneamente no processo simples considerado.", null,
                        "Sempre do maior para o menor", "Tamanho não determina o sentido; diferença de temperatura, sim.", null,
                        "Não há transferência", "Há transferência enquanto existir diferença de temperatura.", null),
                q("Durante a ebulição de água pura a pressão constante, por que a temperatura pode permanecer praticamente constante mesmo recebendo energia?", 1,
                        "Porque a água deixou de receber energia", "A fonte continua fornecendo energia.", null,
                        "Porque a energia está sendo usada na mudança de estado", "Correto: no modelo, trata-se de calor latente.", null,
                        "Porque calor e temperatura são a mesma coisa", "Justamente não são; energia pode ser transferida sem aumentar a temperatura durante a mudança de fase.", null,
                        "Porque o termômetro para de funcionar", "Não é a explicação física.", null)));

        // ========================= QUÍMICA =========================
        add(m, n(
                "q_materia", "Química", "Matéria, substância, mistura e estado físico", 1,
                "Matéria é aquilo que possui massa e ocupa espaço. Substância tem composição característica; mistura reúne duas ou mais substâncias. Estado físico descreve a organização/energia das partículas em sólido, líquido ou gás no modelo básico.",
                "SUBSTÂNCIA = composição característica.\nMISTURA = mais de uma substância.",
                "homogênea = uma fase visível no nível macroscópico\nheterogênea = duas ou mais fases\nfase = porção visualmente uniforme do sistema\nmudança de estado = altera estado físico sem necessariamente formar substância nova",
                "Água destilada é tratada como substância. Água com sal dissolvido é mistura homogênea. Água e óleo formam mistura heterogênea porque apresentam fases distintas.",
                "Gelo derretendo continua sendo H₂O: muda o estado físico, não a identidade química da substância.",
                "Contraexemplo: toda mistura com dois componentes tem duas fases. Sal totalmente dissolvido em água produz uma fase visível.",
                "Erro comum: confundir componente com fase.",
                null,
                q("Qual sistema é uma mistura heterogênea no nível macroscópico?", 3,
                        "água destilada", "É tratada como substância pura nesse contexto.", null,
                        "ar filtrado", "É mistura de gases, mas homogênea macroscopicamente.", null,
                        "água com açúcar totalmente dissolvido", "É mistura homogênea.", null,
                        "água e óleo", "Correto: formam fases distintas.", null),
                q("Um bloco de gelo derrete em um copo fechado. Qual afirmação é correta?", 1,
                        "Forma-se uma nova substância", "A identidade química continua H₂O.", null,
                        "Ocorre mudança física de estado", "Correto.", null,
                        "O número atômico do oxigênio muda", "Mudanças de estado não alteram os elementos.", "q_atomo",
                        "A água deixa de ser matéria", "Continua tendo massa e ocupando espaço.", null)));

        add(m, n(
                "q_transformacao", "Química", "Fenômeno físico e transformação química", 2,
                "Mudança física altera estado, forma ou organização sem produzir necessariamente novas substâncias. Transformação química produz substâncias com composição diferente.",
                "Pergunta-chave: surgiram substâncias novas?",
                "reagentes = substâncias iniciais\nprodutos = substâncias formadas\nevidência = indício observável, como gás, precipitado, mudança persistente de cor/energia\nconservação de massa = em sistema fechado, massa total é conservada",
                "Queimar papel é transformação química: surgem gases, cinzas e outras substâncias. Cortar papel é mudança física: a composição principal permanece.",
                "Dissolver sal em água, em abordagem escolar básica, é mudança física porque o sal pode ser recuperado por evaporação e não exige formação de substância nova.",
                "Contraexemplo: toda mudança de cor prova reação. Cor pode mudar por mistura física; é preciso analisar o contexto.",
                "Erro comum: usar uma única pista como prova absoluta sem considerar reversibilidade, composição e outras evidências.",
                new String[]{"q_materia"},
                q("Qual situação representa mais claramente transformação química?", 0,
                        "ferro enferrujando", "Correto: formam-se óxidos de ferro, novas substâncias.", null,
                        "gelo derretendo", "É mudança de estado.", "q_materia",
                        "papel sendo cortado", "Muda forma, não composição.", null,
                        "água evaporando", "É mudança de estado.", "q_materia"),
                q("Em um recipiente fechado, 10 g de A reagem completamente com 15 g de B. Pela conservação da massa, qual a massa total dos produtos?", 2,
                        "5 g", "Subtrai as massas sem fundamento.", null,
                        "15 g", "Ignora a massa de A.", null,
                        "25 g", "Correto: em sistema fechado, massa total dos reagentes=massas dos produtos.", null,
                        "150 g", "Multiplica valores que deveriam ser somados.", "m_operacoes")));

        add(m, n(
                "q_atomo", "Química", "Átomo: próton, nêutron e elétron", 1,
                "Átomo é uma unidade estrutural da matéria. No modelo escolar, prótons e nêutrons ficam no núcleo; elétrons ocupam a região ao redor.",
                "PRÓTON +1.\nELÉTRON −1.\nNÊUTRON 0.",
                "núcleo = região central com prótons e nêutrons\neletrosfera/nuvem eletrônica = região associada aos elétrons\ncarga elétrica = propriedade positiva, negativa ou neutra\níon = átomo/espécie com desequilíbrio entre prótons e elétrons",
                "Átomo neutro com 8 prótons possui 8 elétrons. Se perder 2 elétrons, passa a ter carga +2, pois ficaram duas cargas positivas a mais.",
                "O número de nêutrons pode variar entre átomos do mesmo elemento; isso gera isótopos.",
                "Contraexemplo: elétron fica no núcleo. No modelo básico, ele está associado à região externa ao núcleo.",
                "Erro comum: trocar número de prótons por número de massa.",
                null,
                q("Um átomo neutro possui 12 prótons. Quantos elétrons possui?", 1,
                        "6", "Não há relação de metade nesse caso.", null,
                        "12", "Correto: neutralidade exige cargas positivas e negativas equilibradas.", null,
                        "24", "Isso dobraria a quantidade de elétrons sem motivo.", null,
                        "0", "Sem elétrons, teria carga positiva intensa.", null),
                q("Uma espécie possui 17 prótons e 18 elétrons. Qual é sua carga líquida?", 3,
                        "+17", "Ignora a carga dos elétrons.", null,
                        "+1", "Há um elétron a mais, então a carga é negativa.", null,
                        "0", "Prótons e elétrons não estão em igual número.", null,
                        "−1", "Correto: 17 cargas + e 18 cargas − resultam em −1.", null)));

        add(m, n(
                "q_numero_atomico", "Química", "Número atômico, massa e isótopos", 2,
                "Número atômico Z é o número de prótons. Número de massa A é prótons+nêutrons. Isótopos têm mesmo Z e diferente número de nêutrons/A.",
                "Z = prótons.\nA = prótons + nêutrons.\nnêutrons = A−Z.",
                "elemento químico = definido pelo número de prótons\nisótopos = mesmo elemento, massas diferentes\nisóbaros = mesmo A, Z diferente\nisótonos = mesmo número de nêutrons",
                "Para um átomo com Z=11 e A=23: prótons=11; nêutrons=23−11=12. Se neutro, elétrons=11.",
                "Carbono-12 e carbono-14 têm Z=6 nos dois casos; diferem em nêutrons: 6 e 8.",
                "Contraexemplo: mudar o número de prótons e continuar sendo o mesmo elemento. Alterar Z muda a identidade do elemento.",
                "Erro comum: calcular nêutrons como Z−A e obter negativo.",
                new String[]{"q_atomo", "m_inteiros"},
                q("Um átomo tem Z=19 e A=39. Quantos nêutrons?", 2,
                        "19", "Esse é o número de prótons.", null,
                        "39", "Esse é o total prótons+nêutrons.", null,
                        "20", "Correto: 39−19=20.", null,
                        "58", "Somou A+Z em vez de subtrair.", null),
                q("Duas espécies têm 8 prótons cada, mas uma possui 8 nêutrons e a outra 10. Elas são:", 0,
                        "isótopos do mesmo elemento", "Correto: mesmo Z, diferentes nêutrons/massas.", null,
                        "elementos diferentes obrigatoriamente", "Elemento é definido pelo número de prótons, que é o mesmo.", null,
                        "isóbaros porque os prótons são iguais", "Isóbaros têm mesmo número de massa, não mesmo Z.", null,
                        "íons de cargas opostas necessariamente", "Não foi informado o número de elétrons.", "q_atomo")));

        // ========================= BIOLOGIA =========================
        add(m, n(
                "b_celula", "Biologia", "Célula: unidade básica dos seres vivos", 1,
                "Célula é a unidade estrutural e funcional básica da vida. Procariontes não possuem núcleo delimitado por membrana; eucariontes possuem.",
                "PROCARIONTE = sem núcleo membranoso.\nEUCARIONTE = com núcleo membranoso.",
                "membrana plasmática = delimita e controla trocas\ncitoplasma = região interna onde ocorrem processos\nnúcleo = compartimento com material genético em eucariontes\norganela = estrutura celular especializada",
                "Bactérias são procariontes: têm DNA, membrana e ribossomos, mas não núcleo membranoso. Células animais e vegetais são eucariontes.",
                "A célula vegetal possui parede celular, cloroplastos e grande vacúolo central, além de estruturas comuns a outras eucariontes.",
                "Contraexemplo: procarionte não tem DNA. Tem DNA; o que falta é núcleo delimitado por membrana.",
                "Erro comum: transformar 'mais simples estruturalmente' em 'sem estruturas internas importantes'.",
                null,
                q("Qual característica distingue diretamente uma célula eucarionte de uma procarionte?", 2,
                        "presença de membrana plasmática", "Ambas possuem membrana plasmática.", null,
                        "presença de DNA", "Ambas possuem material genético.", null,
                        "presença de núcleo delimitado por membrana", "Correto.", null,
                        "capacidade de realizar metabolismo", "Ambas realizam processos metabólicos.", null),
                q("Uma célula possui DNA, ribossomos e membrana, mas não apresenta núcleo membranoso. Ela é mais provavelmente:", 1,
                        "uma célula animal", "Células animais são eucariontes e possuem núcleo.", null,
                        "procarionte", "Correto.", null,
                        "um vírus obrigatoriamente", "Vírus não são células e não apresentam essa organização celular completa.", null,
                        "uma célula vegetal", "Vegetais são eucariontes.", null)));

        add(m, n(
                "b_dna", "Biologia", "DNA, gene, cromossomo e hereditariedade", 2,
                "DNA é a molécula que armazena informação genética. Gene é um trecho funcional de DNA. Cromossomo é uma estrutura organizada que contém DNA associado a proteínas.",
                "DNA = material.\nGENE = trecho de informação.\nCROMOSSOMO = DNA organizado.",
                "alelo = versão de um gene\ngenótipo = conjunto de alelos considerado\nfenótipo = característica observável resultante de genes + ambiente\nhereditariedade = transmissão de informação genética entre gerações",
                "Imagine um livro: DNA é o texto total; genes são trechos/instruções; cromossomos são volumes organizados. A analogia ajuda, mas genes não funcionam isoladamente como frases simples.",
                "Dizer que um fenótipo depende de um gene não significa que ambiente nunca importa. Altura humana, por exemplo, envolve muitos genes e fatores ambientais.",
                "Contraexemplo: 'gene é uma característica visível'. Gene é informação; característica é fenótipo.",
                "Erro comum: tratar genótipo e fenótipo como sinônimos.",
                new String[]{"b_celula"},
                q("Qual relação está correta?", 0,
                        "gene é um segmento de DNA", "Correto.", null,
                        "DNA é uma organela", "DNA é molécula; organela é estrutura celular.", "b_celula",
                        "fenótipo é exatamente o mesmo que genótipo", "Fenótipo é resultado observável, influenciado pelo genótipo e ambiente.", null,
                        "cromossomo não contém DNA", "Cromossomos são estruturas de DNA associado a proteínas.", null),
                q("Dois indivíduos com o mesmo genótipo para uma característica podem apresentar diferenças fenotípicas em alguns casos por quê?", 2,
                        "Porque genes desaparecem depois do nascimento", "Genes não desaparecem dessa forma geral.", null,
                        "Porque cromossomos deixam de conter DNA", "Isso não ocorre em células normais dessa forma.", null,
                        "Porque o ambiente e outros fatores também podem influenciar a expressão da característica", "Correto.", null,
                        "Porque fenótipo nunca depende de genes", "Também é falso; genes podem contribuir fortemente.", null)));

        add(m, n(
                "b_ecologia", "Biologia", "Ecossistema, habitat, nicho e relações", 2,
                "Ecossistema inclui seres vivos e fatores físicos interagindo. Habitat é onde um organismo vive; nicho descreve seu modo de vida e papel ecológico.",
                "HABITAT = onde vive.\nNICHO = como vive e interage.",
                "fatores bióticos = seres vivos/interações\nfatores abióticos = luz, água, temperatura, solo etc.\npopulação = indivíduos da mesma espécie numa área\ncomunidade = conjunto de populações",
                "Duas espécies podem compartilhar habitat e ocupar nichos diferentes, usando recursos distintos ou horários distintos. Isso reduz competição direta.",
                "Manguezal é habitat de muitas espécies; o papel de um caranguejo na decomposição e alimentação faz parte de seu nicho.",
                "Contraexemplo: nicho é apenas o endereço do organismo. Isso é habitat.",
                "Erro comum: esquecer fatores abióticos ao definir ecossistema.",
                null,
                q("Qual alternativa descreve melhor nicho ecológico?", 3,
                        "o continente em que a espécie existe", "Isso descreve localização ampla, não papel ecológico.", null,
                        "apenas o local físico de abrigo", "Isso se aproxima de habitat.", null,
                        "o número total de indivíduos", "Isso é tamanho populacional.", null,
                        "o conjunto de recursos usados, hábitos e interações da espécie", "Correto.", null),
                q("Duas aves vivem na mesma floresta, mas uma se alimenta no topo das árvores de manhã e outra no solo à noite. O exemplo mostra principalmente:", 1,
                        "habitats obrigatoriamente diferentes", "Elas podem compartilhar o mesmo habitat geral.", null,
                        "nichos diferentes dentro de um mesmo habitat", "Correto.", null,
                        "ausência completa de interação ecológica", "Não podemos concluir ausência total de interação.", null,
                        "que pertencem à mesma espécie", "Nada permite concluir isso.", null)));

        // ========================= HISTÓRIA =========================
        add(m, n(
                "h_tempo_fonte", "História", "Tempo histórico, fonte e contexto", 1,
                "História estuda sociedades humanas no tempo usando vestígios e fontes. Fonte não é só documento escrito: objetos, imagens, relatos, edifícios e registros digitais também podem ser analisados.",
                "FONTE = vestígio usado para investigar.\nCONTEXTO = condições de tempo, lugar e sociedade.",
                "fonte primária = produzida no período/por agentes do fenômeno estudado\nfonte secundária = análise posterior\nanacronismo = projetar conceitos/valores de outro tempo sem cuidado\nperiodização = divisão interpretativa do tempo histórico",
                "Uma carta de 1822 pode ser fonte primária para estudar debates daquele período. Um livro atual que analisa a carta é fonte secundária. Nenhuma fonte 'fala sozinha': autoria, finalidade e público importam.",
                "Propaganda política é útil mesmo quando é tendenciosa, pois revela estratégias, valores e disputas. Viés não torna automaticamente a fonte inútil.",
                "Contraexemplo: fonte histórica é apenas texto verdadeiro e neutro. Historiadores também estudam fontes parciais e contraditórias.",
                "Erro comum: julgar uma sociedade passada apenas com categorias atuais sem reconstruir contexto.",
                null,
                q("Qual item pode ser fonte histórica?", 3,
                        "apenas leis oficiais", "Leis são fontes, mas não as únicas.", null,
                        "apenas livros escritos por historiadores", "Também existem fontes produzidas no período estudado.", null,
                        "apenas objetos antigos encontrados em museus", "Objetos são fontes, mas o conjunto é muito mais amplo.", null,
                        "cartas, objetos, fotografias, relatos e registros digitais, dependendo da investigação", "Correto.", null),
                q("Ao analisar um cartaz de propaganda de uma guerra, qual pergunta histórica é mais adequada?", 1,
                        "O cartaz é neutro e descreve tudo exatamente como ocorreu?", "Propaganda tem intenção; neutralidade não deve ser presumida.", null,
                        "Quem o produziu, para qual público e com qual objetivo?", "Correto: autoria, destinatário e finalidade ajudam a interpretar a fonte.", null,
                        "A imagem é bonita?", "Pode ser aspecto estético, mas não é suficiente para análise histórica.", null,
                        "Podemos ignorar o contexto porque a imagem se explica sozinha?", "Contexto é essencial.", null)));

        add(m, n(
                "h_mercantilismo", "História", "Estado moderno e mercantilismo", 2,
                "Mercantilismo é um conjunto variado de práticas econômicas dos Estados europeus modernos, com forte intervenção estatal, proteção comercial, monopólios e busca de acumulação de riqueza.",
                "MERCANTILISMO não é uma receita única; pense em Estado forte + controle do comércio.",
                "protecionismo = barreiras para favorecer produção/comércio interno\nmonopólio = exclusividade de comércio\nmetalismo = valorização de metais preciosos em certas políticas\npacto colonial = restrições comerciais ligando colônia à metrópole",
                "Uma monarquia que aumenta tarifas sobre importados e reserva o comércio colonial a grupos autorizados está adotando práticas associadas ao mercantilismo.",
                "As políticas variaram entre países e períodos. O conceito serve para identificar tendências, não para imaginar que todos aplicaram exatamente o mesmo conjunto de medidas.",
                "Contraexemplo: mercantilismo = livre-comércio irrestrito. Em geral, a lógica era de controle e proteção estatal.",
                "Erro comum: decorar 'ouro' e esquecer as relações entre Estado, comércio e expansão colonial.",
                new String[]{"h_tempo_fonte"},
                q("Qual medida combina mais com práticas mercantilistas?", 0,
                        "tarifas sobre produtos estrangeiros e monopólios comerciais", "Correto.", null,
                        "eliminação completa de fronteiras comerciais pelo Estado", "Isso se aproxima de livre-comércio, não do padrão mercantilista.", null,
                        "fim de qualquer intervenção estatal na economia", "Contradiz a forte atuação estatal típica do conceito.", null,
                        "proibição de comércio colonial pela metrópole", "O comércio colonial era explorado/controlado, não simplesmente abolido.", null),
                q("Por que o pacto colonial se relaciona ao mercantilismo?", 2,
                        "Porque garantia independência econômica total às colônias", "O pacto restringia autonomia comercial.", null,
                        "Porque eliminava monopólios", "Frequentemente fazia o contrário.", null,
                        "Porque direcionava o comércio colonial de forma controlada em benefício da metrópole", "Correto.", null,
                        "Porque impedia a metrópole de participar do comércio", "Contradiz a lógica do sistema.", null)));

        add(m, n(
                "h_industrial", "História", "Revolução Industrial", 2,
                "Revolução Industrial foi um processo de transformação produtiva e social marcado por mecanização, sistema fabril, novas fontes de energia e mudanças nas relações de trabalho.",
                "Não é só 'inventar máquinas': muda produção, trabalho, cidade e sociedade.",
                "mecanização = uso crescente de máquinas\nfábrica = concentração de trabalhadores e meios de produção\nproletariado = trabalhadores assalariados sem controle dos meios de produção no modelo clássico\nurbanização = crescimento da população urbana",
                "A máquina a vapor ampliou a capacidade de produção e transporte em determinados setores, mas a industrialização também envolveu capital, mercados, recursos, mudanças agrícolas e organização do trabalho.",
                "O crescimento industrial atraiu populações para cidades, onde condições de moradia e trabalho frequentemente eram precárias durante etapas iniciais.",
                "Contraexemplo: a industrialização melhorou imediatamente a vida de todos. Houve ganhos produtivos, mas também exploração e conflitos sociais.",
                "Erro comum: tratar o processo como um único evento com uma data exata.",
                new String[]{"h_tempo_fonte"},
                q("Qual transformação está mais diretamente ligada à industrialização clássica?", 1,
                        "retorno generalizado ao artesanato doméstico como forma dominante", "A tendência central foi aumento do sistema fabril/mecanizado.", null,
                        "concentração da produção em fábricas e uso ampliado de máquinas", "Correto.", null,
                        "fim do trabalho assalariado", "O trabalho assalariado se expandiu em muitos setores.", null,
                        "desaparecimento das cidades", "A urbanização cresceu.", null),
                q("Por que a Revolução Industrial é também uma transformação social?", 3,
                        "Porque apenas mudou o formato das máquinas", "Isso seria transformação técnica, mas o processo foi mais amplo.", null,
                        "Porque eliminou todos os conflitos de classe", "Conflitos sociais se intensificaram em muitos contextos.", null,
                        "Porque impediu migrações para cidades", "Houve forte urbanização.", null,
                        "Porque alterou relações de trabalho, padrões urbanos, consumo e organização social", "Correto.", null)));

        // ========================= GEOGRAFIA =========================
        add(m, n(
                "g_mapa_escala", "Geografia", "Mapa, legenda, orientação e escala", 1,
                "Mapa representa o espaço de forma reduzida e seletiva. Legenda explica símbolos; orientação indica direções; escala relaciona medida no mapa à medida real.",
                "MAPA não é o território.\nESCALA = medida no mapa / medida real.",
                "escala numérica = ex. 1:100 000\nescala gráfica = barra graduada\nlegenda = significado dos símbolos\norientação = referência de direção, como norte",
                "Escala 1:100 000 significa que 1 unidade no mapa representa 100 000 da mesma unidade no real. 1 cm → 100 000 cm = 1 km.",
                "Se duas cidades estão a 4,5 cm em mapa 1:200 000: distância real=4,5×200 000 cm=900 000 cm=9 km.",
                "Contraexemplo: escala maior tem denominador maior. Em cartografia, 1:10 000 é escala maior que 1:1 000 000 porque mostra mais detalhe e menor área.",
                "Erro comum: esquecer de converter centímetros para metros/quilômetros.",
                new String[]{"m_razao", "f_unidades"},
                q("Em escala 1:50 000, 2 cm no mapa representam:", 1,
                        "100 m", "2×50 000=100 000 cm=1 000 m, não 100.", null,
                        "1 km", "Correto.", null,
                        "10 km", "Erro de fator 10 na conversão.", null,
                        "50 km", "Confunde o denominador com distância direta em quilômetros.", null),
                q("Qual mapa tende a mostrar MAIS detalhes de um bairro?", 0,
                        "1:5 000", "Correto: menor denominador, maior escala cartográfica, mais detalhe.", null,
                        "1:500 000", "Representa área muito maior com menos detalhe.", null,
                        "1:5 000 000", "É escala pequena, adequada a áreas amplas.", null,
                        "1:50 000 000", "Mostra áreas enormes com pouco detalhe local.", null)));

        add(m, n(
                "g_coordenadas", "Geografia", "Latitude, longitude e coordenadas", 1,
                "Latitude mede distância angular ao Equador; longitude mede distância angular ao meridiano de Greenwich. Juntas, localizam pontos na superfície terrestre.",
                "LATITUDE = norte/sul do Equador.\nLONGITUDE = leste/oeste de Greenwich.",
                "paralelos = linhas de latitude\nmeridianos = linhas de longitude\nEquador = 0° latitude\nGreenwich = 0° longitude",
                "20°S, 45°W significa 20 graus ao sul do Equador e 45 graus a oeste de Greenwich.",
                "Latitude varia de 0° a 90° N/S; longitude de 0° a 180° E/W.",
                "Contraexemplo: longitude indica norte e sul. Isso é latitude.",
                "Erro comum: inverter a ordem e os referenciais.",
                new String[]{"g_mapa_escala"},
                q("Uma cidade em 15°N, 40°W está:", 2,
                        "ao sul do Equador e leste de Greenwich", "N indica norte; W indica oeste.", null,
                        "ao sul do Equador e oeste de Greenwich", "N não é sul.", null,
                        "ao norte do Equador e oeste de Greenwich", "Correto.", null,
                        "ao norte do Equador e leste de Greenwich", "W indica oeste.", null),
                q("Qual linha de referência corresponde a 0° de latitude?", 1,
                        "Meridiano de Greenwich", "É 0° de longitude.", null,
                        "Linha do Equador", "Correto.", null,
                        "Trópico de Capricórnio", "É um paralelo ao sul, não 0°.", null,
                        "Círculo Polar Ártico", "É paralelo de alta latitude norte.", null)));

        add(m, n(
                "g_clima", "Geografia", "Tempo, clima e fatores climáticos", 2,
                "Tempo é o estado atmosférico em curto prazo. Clima é o padrão observado ao longo de períodos longos. Latitude, altitude, relevo, massas de ar, continentalidade e maritimidade influenciam o clima.",
                "TEMPO = hoje/agora.\nCLIMA = padrão de muitos anos.",
                "elementos climáticos = temperatura, umidade, pressão, chuva, vento\nfatores climáticos = condições que influenciam os elementos\namplitude térmica = diferença entre temperaturas máxima e mínima ou entre médias",
                "'Hoje choveu muito' descreve tempo. 'A região possui verões chuvosos e invernos mais secos' descreve padrão climático.",
                "Cidades em maior altitude tendem, em condições comparáveis, a apresentar temperaturas menores porque a temperatura do ar geralmente diminui com a altitude na troposfera.",
                "Contraexemplo: um dia frio prova que o clima da cidade ficou frio. Um evento isolado não define clima.",
                "Erro comum: confundir elemento (temperatura) com fator (altitude).",
                new String[]{"g_mapa_escala"},
                q("Qual afirmação descreve clima, e não apenas tempo?", 3,
                        "Choveu às 15h de ontem", "Evento de curto prazo.", null,
                        "A temperatura agora é 31 °C", "Condição momentânea.", null,
                        "Amanhã pode ocorrer uma frente fria", "Previsão de tempo.", null,
                        "A região apresenta estação chuvosa concentrada no verão ao longo de muitos anos", "Correto.", null),
                q("Duas cidades estão em latitude semelhante, mas uma fica a 1 500 m de altitude e outra ao nível do mar. Em condições comparáveis, qual tendência é plausível?", 0,
                        "A cidade mais alta apresentar temperaturas médias menores", "Correto: altitude é fator climático relevante.", null,
                        "A altitude não influenciar temperatura", "Influencia em muitos contextos.", null,
                        "A cidade mais alta ser sempre mais quente", "Contraria a tendência geral considerada.", null,
                        "Latitude deixar de existir", "Altitude não elimina a posição latitudinal.", null)));

        add(m, n(
                "g_populacao", "Geografia", "População absoluta, densidade e migração", 2,
                "População absoluta é o total de habitantes. Densidade demográfica compara habitantes com área. Migração é deslocamento com mudança de residência em diferentes escalas.",
                "DENSIDADE = habitantes / área.",
                "populoso = muita população absoluta\npovoado = alta densidade\nimigração = deslocamento residencial\nêxodo rural = saída do campo para cidades",
                "Um país pode ser muito populoso e pouco povoado se tiver enorme território. Ex.: 20 milhões de habitantes em 10 milhões de km² → densidade 2 hab/km².",
                "Densidade média não mostra como a população está distribuída internamente; pode haver áreas muito concentradas e outras vazias.",
                "Contraexemplo: 'mais populoso' significa necessariamente 'mais denso'. São conceitos diferentes.",
                "Erro comum: esquecer de dividir pela área ou comparar países de tamanhos muito diferentes só pelo total.",
                new String[]{"m_razao"},
                q("Uma região tem 600 mil habitantes em 20 mil km². Qual a densidade média?", 1,
                        "12 hab/km²", "600 000/20 000=30.", null,
                        "30 hab/km²", "Correto.", null,
                        "300 hab/km²", "Erro de fator 10.", null,
                        "12 000 hab/km²", "Multiplica/erra a escala em vez de dividir.", "m_razao"),
                q("País A tem 50 milhões de habitantes em 500 mil km². País B tem 20 milhões em 100 mil km². Qual é mais povoado?", 2,
                        "A, porque tem mais habitantes", "Isso define ser mais populoso, não necessariamente mais povoado.", null,
                        "Os dois têm a mesma densidade", "A=100 hab/km²; B=200 hab/km².", null,
                        "B, porque sua densidade é maior", "Correto.", null,
                        "Não é possível calcular", "Há população e área para os dois.", null)));

        add(m, n(
                "g_globalizacao", "Geografia", "Globalização, redes e divisão do trabalho", 3,
                "Globalização intensifica fluxos de mercadorias, capitais, informação, pessoas e produção entre lugares. Não elimina fronteiras nem desigualdades.",
                "GLOBALIZAÇÃO = redes mais conectadas, não mundo sem diferenças.",
                "cadeia produtiva global = etapas em diferentes países/regiões\ntransnacional = empresa que opera em vários países\nfluxo = movimento entre lugares\nD.I.T. = divisão internacional do trabalho",
                "Um celular pode ser projetado em um país, usar componentes de vários outros, ser montado em outro e vendido mundialmente. Isso evidencia cadeia produtiva articulada em rede.",
                "Conectividade é desigual: alguns territórios concentram comando, tecnologia e capital; outros entram principalmente como fornecedores de matérias-primas ou mão de obra.",
                "Contraexemplo: globalização significa que todos os países ganham da mesma forma. Benefícios e custos são distribuídos desigualmente.",
                "Erro comum: reduzir globalização à internet; ela também envolve produção, finanças, logística e poder político.",
                new String[]{"g_mapa_escala", "g_populacao"},
                q("Qual situação evidencia melhor uma cadeia produtiva global?", 0,
                        "produto projetado num país, componentes fabricados em vários e montagem final em outro", "Correto.", null,
                        "uma família produz apenas para consumo próprio", "É produção local e autoconsumo.", null,
                        "uma cidade bloqueia todas as comunicações externas", "É desconexão, não integração em rede.", null,
                        "uma feira vende apenas produtos do próprio bairro", "Pode ser atividade econômica, mas não evidencia cadeia global.", null),
                q("Por que globalização não significa fim das desigualdades territoriais?", 3,
                        "Porque fluxos globais deixaram de existir", "Eles existem e se intensificaram em muitos setores.", null,
                        "Porque todos os territórios possuem o mesmo papel", "Justamente possuem papéis e poder diferentes.", null,
                        "Porque empresas transnacionais operam em apenas um país", "Por definição, operam além de um país.", null,
                        "Porque infraestrutura, capital, tecnologia e poder de decisão permanecem distribuídos de forma desigual", "Correto.", null)));

        return m;
    }

    public static String subjectIntro(String subject) {
        switch (subject) {
            case "Português":
                return "Português no CEFET exige duas coisas juntas: compreender textos e perceber como escolhas de palavras/estruturas produzem sentido. A trilha começa em classes e estrutura da oração porque termos como sujeito, concordância, pronome e inferência deixam de ser 'palavras misteriosas' quando a base está clara.";
            case "Matemática":
                return "Matemática é uma cadeia. Fração sustenta razão; razão sustenta porcentagem; distributiva sustenta álgebra; álgebra sustenta equações; equações sustentam funções. O tutor volta exatamente um elo quando detecta uma lacuna.";
            case "Física":
                return "Física combina interpretação do fenômeno, grandezas, unidades e modelo matemático. A fórmula entra depois de identificar o que está acontecendo e quais dados representam cada grandeza.";
            case "Química":
                return "Química alterna duas escalas: o que observamos no mundo macroscópico e o modelo de partículas usado para explicar. O tutor separa mudança física, transformação química, átomo e composição para evitar mistura de conceitos.";
            case "Biologia":
                return "Biologia fica mais fácil quando cada assunto é ligado por estrutura → função → consequência. Célula sustenta genética; organismo e ambiente sustentam ecologia.";
            case "História":
                return "História não é apenas decorar datas. As questões pedem contexto, agentes, interesses, causas, consequências e leitura de fontes. A trilha começa aprendendo a interpretar fonte e tempo histórico.";
            case "Geografia":
                return "Geografia pergunta onde, em qual escala, como os fenômenos se distribuem e por quê. Mapas, razões, fluxos, população e clima aparecem como relações espaciais, não listas soltas.";
            default:
                return "Trilha organizada por pré-requisitos e transferência.";
        }
    }

    public static LinkedHashMap<String, String> glossary() {
        LinkedHashMap<String, String> g = new LinkedHashMap<>();
        g.put("singular", "Forma gramatical usada para uma unidade: 'a questão', 'o aluno'.");
        g.put("plural", "Forma gramatical usada para mais de uma unidade: 'as questões', 'os alunos'.");
        g.put("substantivo", "Palavra que funciona como nome de ser, coisa, lugar, ação, estado, sentimento ou ideia.");
        g.put("adjetivo", "Palavra que atribui característica, estado, origem ou qualidade a um substantivo.");
        g.put("artigo", "Palavra como o/a/os/as/um/uma que acompanha um substantivo e ajuda a determiná-lo.");
        g.put("verbo", "Palavra que organiza a oração e pode expressar ação, estado, mudança, fenômeno ou processo.");
        g.put("sujeito", "Termo com o qual o verbo normalmente concorda e sobre o qual se faz uma declaração.");
        g.put("predicado", "Parte da oração que contém o verbo e aquilo que se declara sobre o sujeito.");
        g.put("inferência", "Conclusão sustentada por pistas do texto, mesmo sem estar escrita literalmente.");
        g.put("tese", "Ideia central defendida em um texto argumentativo.");
        g.put("argumento", "Razão usada para sustentar uma tese.");
        g.put("evidência", "Dado, fato, exemplo ou fonte que sustenta um argumento.");
        g.put("incógnita", "Valor desconhecido que se deseja encontrar em uma equação.");
        g.put("variável", "Símbolo, geralmente letra, que pode representar valores.");
        g.put("equação", "Igualdade matemática que contém valor desconhecido e precisa ser satisfeita.");
        g.put("função", "Relação que associa cada entrada permitida a exatamente uma saída.");
        g.put("domínio", "Conjunto das entradas permitidas de uma função.");
        g.put("imagem", "Conjunto das saídas que a função realmente produz.");
        g.put("contradomínio", "Conjunto definido como destino possível das saídas de uma função.");
        g.put("razão", "Comparação entre duas quantidades por divisão.");
        g.put("proporção", "Igualdade entre duas razões.");
        g.put("porcentagem", "Razão expressa por 100; 15%=15/100.");
        g.put("massa", "Grandeza associada à quantidade de matéria/inércia; no SI, quilograma.");
        g.put("força", "Interação capaz de alterar o movimento; unidade SI: newton.");
        g.put("átomo", "Unidade estrutural da matéria formada por núcleo e região eletrônica no modelo escolar.");
        g.put("isótopo", "Átomos do mesmo elemento, mesmo número de prótons e diferentes números de nêutrons.");
        g.put("célula", "Unidade estrutural e funcional básica dos seres vivos.");
        g.put("habitat", "Lugar onde uma espécie/organismo vive.");
        g.put("nicho", "Modo de vida, recursos e interações de uma espécie no ecossistema.");
        g.put("fonte histórica", "Vestígio usado para investigar sociedades no tempo.");
        g.put("mercantilismo", "Conjunto de práticas econômicas de Estados modernos com controle/proteção do comércio e outras políticas.");
        g.put("escala", "Relação entre medida representada num mapa/desenho e a medida real.");
        g.put("latitude", "Distância angular norte/sul em relação ao Equador.");
        g.put("longitude", "Distância angular leste/oeste em relação a Greenwich.");
        return g;
    }

    private static void add(Map<String, Node> m, Node n) { m.put(n.id, n); }

    private static Node n(String id, String subject, String title, int level,
                          String summary, String memory, String terms, String detail,
                          String worked, String counter, String trap, String[] prereq,
                          Question check, Question transfer) {
        return new Node(id, subject, title, level, summary, memory, terms, detail, worked,
                counter, trap, prereq, check, transfer);
    }

    private static Question q(String prompt, int correct,
                              String o0, String f0, String r0,
                              String o1, String f1, String r1,
                              String o2, String f2, String r2,
                              String o3, String f3, String r3) {
        return new Question(prompt, correct,
                new String[]{o0,o1,o2,o3},
                new String[]{f0,f1,f2,f3},
                new String[]{r0,r1,r2,r3});
    }
}
