package mktd6.trader;

import mktd6.model.trader.Trader;
import mktd6.trader.helper.TraderStores;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Optional;

public class TraderInvestmentTransformer implements Transformer<Trader, Double, KeyValue<Trader, Double>> {

    private KeyValueStore<Trader, Double> stateStore;
    private ProcessorContext context;
    private final Trader trader;

    public TraderInvestmentTransformer(Trader trader) {
        this.trader = trader;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        stateStore = (KeyValueStore) this.context.getStateStore(TraderStores.TRADER_INVESTMENT_STORE.getStoreName());
        this.context.schedule(
            1000,
            PunctuationType.WALL_CLOCK_TIME,
            time -> {
                Optional.ofNullable(stateStore.get(trader))
                    .ifPresent(v -> context.forward(trader, v));
                stateStore.delete(trader);
            });
    }

    @Override
    public KeyValue<Trader, Double> transform(Trader trader, Double value) {
        stateStore.put(trader, value);
        return null;
    }

    @Override
    public KeyValue<Trader, Double> punctuate(long timestamp) {
        return null;
    }

    @Override
    public void close() {}
}
