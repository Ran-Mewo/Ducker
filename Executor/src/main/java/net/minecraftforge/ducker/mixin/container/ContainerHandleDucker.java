package net.minecraftforge.ducker.mixin.container;

import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.service.MixinService;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ContainerHandleDucker  extends ContainerHandleVirtual {

    /**
     * Container handle for nio resources offered by ModLauncher
     */
    static class Resource extends ContainerHandleURI {

        private String name;
        private Path path;

        public Resource(String name, Path path) {
            super(path.toUri());
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return this.name;
        }

        public Path getPath() {
            return this.path;
        }

        @Override
        public String toString() {
            return String.format("ContainerHandleDucker.Resource(%s:%s)", this.name, this.path);
        }

    }

    public ContainerHandleDucker(String name) {
        super(name);
    }

    /**
     * Add a resource to to this container
     *
     * @param name Resource name
     * @param path Resource path
     */
    public void addResource(String name, Path path) {
        this.add(new Resource(name, path));
    }

    /**
     * Add a resource to to this container
     *
     * @param entry Resource entry
     */
    public void addResource(Map.Entry<String, Path> entry) {
        this.add(new Resource(entry.getKey(), entry.getValue()));
    }

    /**
     * Add a resource to to this container
     *
     * @param resource Resource
     */
    @SuppressWarnings("unchecked")
    public void addResource(Object resource) {
        if (resource instanceof Map.Entry) {
            this.addResource((Map.Entry<String, Path>)resource);
        } else {
            MixinService.getService().getLogger("mixin").error("Unrecognised resource type {} passed to {}", resource.getClass(), this);
        }
    }

    /**
     * Add a collection of resources to this container
     *
     * @param resources Resources to add
     */
    public void addResources(List<?> resources) {
        for (Object resource : resources) {
            this.addResource(resource);
        }
    }

    @Override
    public String toString() {
        return String.format("ModLauncher Root Container(%s:%x)", this.getName(), this.hashCode());
    }

}
