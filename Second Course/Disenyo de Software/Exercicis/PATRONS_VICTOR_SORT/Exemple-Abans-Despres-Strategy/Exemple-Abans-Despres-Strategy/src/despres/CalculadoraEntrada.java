package despres;

public class CalculadoraEntrada {
    private CalculadoraEntradaStrategy strategy = null;

    public CalculadoraEntrada() {
    }

    public void setStrategy(CalculadoraEntradaStrategy strategy) {
        this.strategy = strategy;
    }

    public String mostrarPerOnEntrar(){
        return this.strategy.mostrarPerOnEntrar();
    }
}
