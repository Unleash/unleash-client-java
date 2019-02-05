package no.finn.unleash;

public class VariantDefinition {

    private final String name;
    private final int weight;
    private final String payload;

    public VariantDefinition(String name, int weight, String payload) {
        this.name = name;
        this.weight = weight;
        this.payload = payload;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "VariantDefinition{" +
            "name='" + name + '\'' +
            ", weight=" + weight +
            ", payload='" + payload + '\'' +
            '}';
    }
}
