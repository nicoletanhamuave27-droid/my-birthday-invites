package hash;

import modelo.Entrada;
import modelo.Estudante;

/**
 * Tabela Hash com enderecamento aberto e Double Hashing.
 * Sondagem: h(k,i) = (h1(k) + i * h2(k)) mod m
 */
public class TabelaHash {

    public static final int CAPACIDADE_INICIAL = 1009;
    private static final double FATOR_MAXIMO = 0.70;

    private Entrada[] tabela;
    private int capacidade;       // m
    private int n;                // posicoes OCUPADAS
    private int capacidadeInicial;

    // contadores (apenas das insercoes pedidas, nao das reinsercoes do redimensionamento)
    private int insercoes;
    private long colisoes;
    private long sondagens;
    private int maiorSondagem;
    private int redimensionamentos;

    public TabelaHash(int capacidadeInicial) {
        capacidade = proximoPrimo(capacidadeInicial);
        this.capacidadeInicial = capacidade;
        tabela = criarTabela(capacidade);
    }

    private Entrada[] criarTabela(int m) {
        Entrada[] t = new Entrada[m];
        for (int i = 0; i < m; i++) {
            t[i] = new Entrada();
        }
        return t;
    }

    // ---------- funcoes hash ----------

    private long chave(String codigo) {
        return Long.parseLong(codigo);
    }

    /** h1(k) = k mod m  (posicao inicial) */
    public int h1(String codigo) {
        return (int) (chave(codigo) % capacidade);
    }

    /** h2(k) = 1 + (k mod (m-1))  (passo, nunca 0) */
    public int h2(String codigo) {
        return 1 + (int) (chave(codigo) % (capacidade - 1));
    }

    private int posicao(int h1, int h2, int i) {
        return (int) (((long) h1 + (long) i * h2) % capacidade);
    }

    // ---------- operacoes ----------

    /** Devolve false se o codigo ja existe. */
    public boolean inserir(Estudante e) {
        boolean inseriu = inserirNaTabela(e, true);
        if (inseriu && fatorCarga() > FATOR_MAXIMO) {
            redimensionar();
        }
        return inseriu;
    }

    private boolean inserirNaTabela(Estudante e, boolean contar) {
        String codigo = e.getCodigo();
        int h1 = h1(codigo);
        int h2 = h2(codigo);

        int livre = -1;          // primeira posicao VAZIO ou REMOVIDO
        int sondagensAteLivre = 0;

        for (int i = 0; i < capacidade; i++) {
            int indice = posicao(h1, h2, i);
            Entrada en = tabela[indice];

            if (en.estado == Entrada.OCUPADO) {
                if (en.estudante.getCodigo().equals(codigo)) {
                    return false;                 // codigo duplicado
                }
            } else {
                if (livre == -1) {
                    livre = indice;
                    sondagensAteLivre = i + 1;
                }
                if (en.estado == Entrada.VAZIO) {
                    break;                        // fim da cadeia: nao ha duplicado
                }
            }
        }

        if (livre == -1) {
            return false;                         // tabela cheia
        }

        tabela[livre].estudante = e;
        tabela[livre].estado = Entrada.OCUPADO;
        n++;

        if (contar) {
            insercoes++;
            sondagens += sondagensAteLivre;
            colisoes += sondagensAteLivre - 1;
            if (sondagensAteLivre > maiorSondagem) {
                maiorSondagem = sondagensAteLivre;
            }
        }
        return true;
    }

    public Estudante pesquisar(String codigo) {
        int h1 = h1(codigo);
        int h2 = h2(codigo);

        for (int i = 0; i < capacidade; i++) {
            Entrada en = tabela[posicao(h1, h2, i)];

            if (en.estado == Entrada.VAZIO) {
                return null;                      // fim da cadeia
            }
            if (en.estado == Entrada.OCUPADO
                    && en.estudante.getCodigo().equals(codigo)) {
                return en.estudante;
            }
            // REMOVIDO ou outro codigo: continua
        }
        return null;
    }

    public boolean remover(String codigo) {
        int h1 = h1(codigo);
        int h2 = h2(codigo);

        for (int i = 0; i < capacidade; i++) {
            Entrada en = tabela[posicao(h1, h2, i)];

            if (en.estado == Entrada.VAZIO) {
                return false;
            }
            if (en.estado == Entrada.OCUPADO
                    && en.estudante.getCodigo().equals(codigo)) {
                en.estado = Entrada.REMOVIDO;     // nunca VAZIO
                en.estudante = null;
                n--;
                return true;
            }
        }
        return false;
    }

    // ---------- redimensionamento ----------

    private void redimensionar() {
        Entrada[] antiga = tabela;
        int capacidadeAntiga = capacidade;

        capacidade = proximoPrimo(2 * capacidadeAntiga);
        tabela = criarTabela(capacidade);
        n = 0;

        // cada elemento e reinserido atraves das funcoes hash (com o novo m)
        for (int i = 0; i < capacidadeAntiga; i++) {
            if (antiga[i].estado == Entrada.OCUPADO) {
                inserirNaTabela(antiga[i].estudante, false);
            }
        }
        redimensionamentos++;
    }

    // ---------- consultas ----------

    public int tamanho()               { return n; }
    public int getCapacidade()         { return capacidade; }
    public int getCapacidadeInicial()  { return capacidadeInicial; }
    public double fatorCarga()         { return (double) n / capacidade; }
    public long getColisoes()          { return colisoes; }
    public long getSondagens()         { return sondagens; }
    public int getMaiorSondagem()      { return maiorSondagem; }
    public int getRedimensionamentos() { return redimensionamentos; }

    public double mediaSondagens() {
        if (insercoes == 0) {
            return 0;
        }
        return (double) sondagens / insercoes;
    }

    /** Devolve todos os estudantes guardados na tabela. */
    public Estudante[] listarTodos() {
        Estudante[] resultado = new Estudante[n];
        int k = 0;
        for (int i = 0; i < capacidade; i++) {
            if (tabela[i].estado == Entrada.OCUPADO) {
                resultado[k] = tabela[i].estudante;
                k++;
            }
        }
        return resultado;
    }

    /** Mostra a tabela (para a demonstracao na defesa). */
    public void exibir() {
        for (int i = 0; i < capacidade; i++) {
            Entrada en = tabela[i];
            String texto;
            if (en.estado == Entrada.VAZIO) {
                texto = "VAZIO";
            } else if (en.estado == Entrada.REMOVIDO) {
                texto = "REMOVIDO";
            } else {
                texto = en.estudante.getCodigo();
            }
            System.out.println(i + " -> " + texto);
        }
    }

    // ---------- numeros primos ----------

    private static boolean ehPrimo(int x) {
        if (x < 2) {
            return false;
        }
        for (int d = 2; (long) d * d <= x; d++) {
            if (x % d == 0) {
                return false;
            }
        }
        return true;
    }

    private static int proximoPrimo(int x) {
        while (!ehPrimo(x)) {
            x++;
        }
        return x;
    }
}
