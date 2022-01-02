package net.minecraftforge.ducker.mixin;

import net.minecraftforge.ducker.util.TypesafeMap;
import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Global property service backed by a type-safe-map
 */
public class Blackboard implements IGlobalPropertyService {

    /**
     * Type safe property key
     * 
     * @param <V> value type
     */
    static class Key<V> implements IPropertyKey {
        
        final TypesafeMap.Key<V> key;
        
        public Key(TypesafeMap owner, String name, Class<V> clazz) {
            this.key = TypesafeMap.Key.getOrCreate(owner, name, clazz);
        }
        
    }
    
    private final Map<String, IPropertyKey> keys = new HashMap<>();
    
    private final TypesafeMap blackboard = new TypesafeMap();

    public Blackboard() {
    }

    @Override
    public IPropertyKey resolveKey(String name) {
        return this.keys.computeIfAbsent(name, key -> new Key<>(this.blackboard, key, Object.class));
    }

    @Override
    public <T> T getProperty(IPropertyKey key) {
        return this.getProperty(key, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setProperty(IPropertyKey key, Object value) {
        this.blackboard.computeIfAbsent(((Key<Object>)key).key, k -> value);
    }

    @Override
    public String getPropertyString(IPropertyKey key, String defaultValue) {
        return this.getProperty(key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(IPropertyKey key, T defaultValue) {
        return this.blackboard.get(((Key<T>)key).key).orElse(defaultValue);
    }

}
