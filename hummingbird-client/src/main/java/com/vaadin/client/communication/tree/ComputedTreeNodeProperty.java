package com.vaadin.client.communication.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.HandlerRegistration;
import com.vaadin.client.communication.tree.CallbackQueue.NodeChangeEvent;

import elemental.json.JsonObject;

public class ComputedTreeNodeProperty extends TreeNodeProperty {

    private final String code;

    private boolean dirty = true;

    private final Collection<HandlerRegistration> dependencies = new HashSet<>();

    private final TreeNodePropertyValueChangeListener dependencyListener = new TreeNodePropertyValueChangeListener() {
        @Override
        public void changeValue(Object oldValue, Object newValue) {
            Object oldOwnValue = getValue();
            dirty = true;
            clearAllDependencies();
            getOwner().getCallbackQueue().enqueue(new NodeChangeEvent() {
                @Override
                public JsonObject serialize() {
                    // Computed values are not serialized to the server
                    return null;
                }

                @Override
                public void dispatch() {
                    List<TreeNodePropertyValueChangeListener> listeners = getListeners();
                    if (listeners != null) {
                        Object newOwnValue = getValue();
                        for (TreeNodePropertyValueChangeListener listener : listeners) {
                            listener.changeValue(oldOwnValue, newOwnValue);
                        }
                    }
                }
            });
        }
    };

    public ComputedTreeNodeProperty(TreeNode owner, String name, String code) {
        super(owner, name);
        this.code = code;

        // Compute initial value
        compute();
    }

    @Override
    public JavaScriptObject getPropertyDescriptor() {
        JavaScriptObject descriptor = super.getPropertyDescriptor();
        removeSetter(descriptor);
        return descriptor;
    }

    @Override
    public Object getValue() {
        if (dirty) {
            compute();
        }
        return super.getValue();
    }

    private void clearAllDependencies() {
        for (HandlerRegistration dependency : dependencies) {
            dependency.removeHandler();
        }
        dependencies.clear();
    }

    private void compute() {
        clearAllDependencies();

        Map<String, JavaScriptObject> context = new HashMap<>();
        context.put("model", getOwner().getProxy());

        // Should maybe be refactored to use Reactive.keepUpToDate
        Collection<TreeNodeProperty> accessedProperies = Reactive
                .collectAccessedProperies(() -> {
                    JavaScriptObject newValue = TreeUpdater
                            .evalWithContext(context, "return " + code);

                    dirty = false;
                    // Bypass own setValue since it always throws
                    super.setValue(newValue);
                });

        for (TreeNodeProperty treeNodeProperty : accessedProperies) {
            dependencies.add(treeNodeProperty
                    .addPropertyChangeListener(dependencyListener));
        }
    }

    @Override
    public void setValue(Object value) {
        throw new IllegalStateException(
                "Can't set value of read-only property " + getName());
    }

    private static native void removeSetter(JavaScriptObject descriptor)
    /*-{
        delete descriptor.set;
    }-*/;

}