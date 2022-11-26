package net.minecraftforge.ducker.transformers;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.util.Constants;
import org.spongepowered.asm.util.SignaturePrinter;
import org.spongepowered.asm.util.asm.ASM;
import org.spongepowered.asm.util.asm.MethodVisitorEx;

import java.util.HashSet;
import java.util.Set;

public class ArgsClassStripper implements IMixinMethodAwareTransformer {

    @Override
    public ClassVisitor transform(ClassNode node, ClassVisitor previous) {
        final ArgsClassRenamer remapper = new ArgsClassRenamer(node);

        final ClassVisitor argsClassRenamer = new ClassRemapper(previous, remapper);
        return new ArgsClassClassVisitor(argsClassRenamer, remapper, node);
    }

    private static final class ArgsClassRenamer extends Remapper {

        private final ClassNode targetNode;

        private final Set<String> innerNames = new HashSet<>();

        private ArgsClassRenamer(ClassNode targetNode) {
            this.targetNode = targetNode;
        }

        @Override
        public String map(String internalName) {
            if (internalName.startsWith("org/spongepowered/asm/synthetic/args/Args")) {
                final String innerName = targetNode.name + "$Args" + internalName.replace("org/spongepowered/asm/synthetic/args/Args$", "");
                innerNames.add(innerName);
                return innerName;
            }

            if (internalName.startsWith("org/spongepowered/asm/mixin/injection/invoke/arg/Args")) {
                return targetNode.name + "$ArgsBase";
            }

            return internalName;
        }
    }

    private static final class ArgsClassClassVisitor extends ClassVisitorWithAdditionalGenerator {

        private final ClassNode classNode;
        private final ArgsClassRenamer renamer;
        private final Set<ClassNode> generatedSpecificArgsClasses = new HashSet<>();

        public ArgsClassClassVisitor(ClassVisitor classVisitor, ArgsClassRenamer remapper, ClassNode classNode) {
            super(classVisitor);
            this.classNode = classNode;
            this.renamer = remapper;
        }

        @Override
        public void visitOuterClass(String owner, String name, String descriptor) {
            super.visitOuterClass(owner, name, descriptor);
        }

        @Override
        public Set<ClassNode> getAdditionalClasses() {
            if (renamer.innerNames.isEmpty()) {
                return Set.of();
            }

            final Set<ClassNode> additionalClasses = new HashSet<>();
            additionalClasses.add(new ArgsBaseClassGenerator(classNode.name).generate());
            additionalClasses.addAll(generatedSpecificArgsClasses);

            return additionalClasses;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            final MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);

            return new MethodVisitor(ASM.API_VERSION, methodVisitor) {

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    if (opcode == Opcodes.INVOKESTATIC && owner.startsWith("org/spongepowered/asm/synthetic/args/Args$") && name.equals("of")) {
                        final String innerName = classNode.name + "$Args" + owner.replace("org/spongepowered/asm/synthetic/args/Args$", "");

                        final ClassNode node = new SpecificArgsClassGenerator().generate(classNode.name, innerName, innerName.replace(classNode.name + "$", ""), descriptor);
                        generatedSpecificArgsClasses.add(node);
                    }

                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                }
            };
        }

        @Override
        public void visitEnd() {
            getAdditionalClasses().forEach(innerClass -> {
                super.visitInnerClass(innerClass.name, classNode.name, innerClass.name.replace(innerClass.outerClass, ""), Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
            });

            super.visitEnd();
        }
    }

    public static final class ArgsBaseClassGenerator implements Opcodes {

        private final String ownerName;
        private final String className;
        private final String descriptor;

        public ArgsBaseClassGenerator(String outerClassName) {
            this.ownerName = outerClassName;
            this.className = outerClassName + "$ArgsBase";
            this.descriptor = "L" + className + ";";
        }

        public ClassNode generate() {

            ClassWriter classWriter = new ClassWriter(0);
            FieldVisitor fieldVisitor;
            MethodVisitor methodVisitor;

            classWriter.visit(V1_6, ACC_PUBLIC | ACC_SUPER | ACC_ABSTRACT, className, null, "java/lang/Object", null);

            classWriter.visitSource("Args.java", null);

            classWriter.visitOuterClass(ownerName, "ArgsBase", "");

            {
                fieldVisitor = classWriter.visitField(ACC_PROTECTED | ACC_FINAL, "values", "[Ljava/lang/Object;", null, null);
                fieldVisitor.visitEnd();
            }
            {
                methodVisitor = classWriter.visitMethod(ACC_PROTECTED, "<init>", "([Ljava/lang/Object;)V", null, null);
                methodVisitor.visitCode();
                Label label0 = new Label();
                methodVisitor.visitLabel(label0);
                methodVisitor.visitLineNumber(46, label0);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                Label label1 = new Label();
                methodVisitor.visitLabel(label1);
                methodVisitor.visitLineNumber(47, label1);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitVarInsn(ALOAD, 1);
                methodVisitor.visitFieldInsn(PUTFIELD, className, "values", "[Ljava/lang/Object;");
                Label label2 = new Label();
                methodVisitor.visitLabel(label2);
                methodVisitor.visitLineNumber(48, label2);
                methodVisitor.visitInsn(RETURN);
                Label label3 = new Label();
                methodVisitor.visitLabel(label3);
                methodVisitor.visitLocalVariable("this", descriptor, null, label0, label3, 0);
                methodVisitor.visitLocalVariable("values", "[Ljava/lang/Object;", null, label0, label3, 1);
                methodVisitor.visitMaxs(2, 2);
                methodVisitor.visitEnd();
            }
            {
                methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "size", "()I", null, null);
                methodVisitor.visitCode();
                Label label0 = new Label();
                methodVisitor.visitLabel(label0);
                methodVisitor.visitLineNumber(56, label0);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitFieldInsn(GETFIELD, className, "values", "[Ljava/lang/Object;");
                methodVisitor.visitInsn(ARRAYLENGTH);
                methodVisitor.visitInsn(IRETURN);
                Label label1 = new Label();
                methodVisitor.visitLabel(label1);
                methodVisitor.visitLocalVariable("this", descriptor, null, label0, label1, 0);
                methodVisitor.visitMaxs(1, 1);
                methodVisitor.visitEnd();
            }
            {
                methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "get", "(I)Ljava/lang/Object;", "<T:Ljava/lang/Object;>(I)TT;", null);
                methodVisitor.visitCode();
                Label label0 = new Label();
                methodVisitor.visitLabel(label0);
                methodVisitor.visitLineNumber(70, label0);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitFieldInsn(GETFIELD, className, "values", "[Ljava/lang/Object;");
                methodVisitor.visitVarInsn(ILOAD, 1);
                methodVisitor.visitInsn(AALOAD);
                methodVisitor.visitInsn(ARETURN);
                Label label1 = new Label();
                methodVisitor.visitLabel(label1);
                methodVisitor.visitLocalVariable("this", descriptor, null, label0, label1, 0);
                methodVisitor.visitLocalVariable("index", "I", null, label0, label1, 1);
                methodVisitor.visitMaxs(2, 2);
                methodVisitor.visitEnd();
            }
            {
                methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, "set", "(ILjava/lang/Object;)V", "<T:Ljava/lang/Object;>(ITT;)V", null);
                methodVisitor.visitEnd();
            }
            {
                methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_VARARGS | ACC_ABSTRACT, "setAll", "([Ljava/lang/Object;)V", null, null);
                methodVisitor.visitEnd();
            }
            classWriter.visitEnd();

            final byte[] payload = classWriter.toByteArray();
            final ClassReader classReader = new ClassReader(payload);
            final ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            return classNode;
        }
    }

    public static final class SpecificArgsClassGenerator implements Opcodes {
        public static final String GETTER_PREFIX = "$";

        private static final String OBJECT = "java/lang/Object";
        private static final String OBJECT_ARRAY = "[L" + OBJECT + ";";

        private static final String VALUES_FIELD = "values";

        private static final String CTOR_DESC = "(" + OBJECT_ARRAY + ")V";

        private static final String SET = "set";
        private static final String SET_DESC = "(ILjava/lang/Object;)V";

        private static final String SETALL = "setAll";
        private static final String SETALL_DESC = "([Ljava/lang/Object;)V";

        private static final String NPE = "java/lang/NullPointerException";
        private static final String NPE_CTOR_DESC = "(Ljava/lang/String;)V";

        private static final String IOOBE = "java/lang/IndexOutOfBoundsException";
        private static final String IOOBE_CTOR_DESC = "(I)V";

        private static final String IAE = "java/lang/IllegalArgumentException";
        private static final String IAE_CTOR_DESC = "(Ljava/lang/String;)V";


        /**
         * Synthetic class info for args class
         */
        static class ArgsClassInfo {
            final String name;

            final String desc;

            final Type[] args;

            ArgsClassInfo(String name, String desc) {
                this.desc = desc;
                this.args = Type.getArgumentTypes(desc);
                this.name = name;
            }

            String getSignature() {
                return new SignaturePrinter("", null, this.args).setFullyQualified(true).getFormattedArgs();
            }

            public String getName() {
                return name;
            }
        }

        public ClassNode generate(String ownerName, String name, String normalClassName, String desc) {
            ArgsClassInfo info = new ArgsClassInfo(name, desc);

            ClassWriter visitor = new ClassWriter(ASM.API_VERSION);

            visitor.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, info.getName(), null, ownerName + "$ArgsBase", null);
            visitor.visitSource(name.substring(name.lastIndexOf('.') + 1) + ".java", null);
            visitor.visitOuterClass(ownerName, normalClassName, "");

            this.generateCtor(visitor, ownerName);
            this.generateToString(info, visitor);
            this.generateFactory(info, visitor);
            this.generateSetters(info, visitor);
            this.generateGetters(info, visitor);

            visitor.visitEnd();

            final byte[] payload = visitor.toByteArray();
            final ClassReader classReader = new ClassReader(payload);
            final ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            return classNode;
        }

        /**
         * Generate the constructor for the subclass, the ctor simply calls the
         * superclass ctor and does nothing else besides
         *
         * @param writer Class writer
         */
        private void generateCtor(ClassVisitor writer, String ownerName) {
            MethodVisitor ctor = writer.visitMethod(Opcodes.ACC_PRIVATE, Constants.CTOR, CTOR_DESC, null, null);
            ctor.visitCode();
            ctor.visitVarInsn(Opcodes.ALOAD, 0);
            ctor.visitVarInsn(Opcodes.ALOAD, 1);
            ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, ownerName + "$ArgsBase", Constants.CTOR, CTOR_DESC, false);
            ctor.visitInsn(Opcodes.RETURN);
            ctor.visitMaxs(2, 2);
            ctor.visitEnd();
        }

        /**
         * Generate a toString method for this Args class.
         *
         * @param writer Class writer
         */
        private void generateToString(ArgsClassInfo info, ClassVisitor writer) {
            MethodVisitor toString = writer.visitMethod(Opcodes.ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
            toString.visitCode();
            toString.visitLdcInsn("Args" + info.getSignature());
            toString.visitInsn(Opcodes.ARETURN);
            toString.visitMaxs(1, 1);
            toString.visitEnd();
        }

        /**
         * Generate the factory method (<tt>of</tt>) for the subclass, the factory
         * method takes the arguments which would have been passed to the target
         * method, marshals them into an <tt>Object[]</tt> array, and then calls the
         * constructor.
         *
         * @param writer Class writer
         */
        private void generateFactory(ArgsClassInfo info, ClassVisitor writer) {
            String ref = info.getName();
            String factoryDesc = Bytecode.changeDescriptorReturnType(info.desc, "L" + ref + ";");
            MethodVisitorEx of = new MethodVisitorEx(writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "of", factoryDesc, null, null));
            of.visitCode();

            // Create args subclass
            of.visitTypeInsn(Opcodes.NEW, ref);
            of.visitInsn(Opcodes.DUP);

            // Create Object[] array to contain args
            of.visitConstant((byte)info.args.length);
            of.visitTypeInsn(Opcodes.ANEWARRAY, OBJECT);

            // Iterate over args and stuff them into the array
            for (byte index = 0, argIndex = 0; index < info.args.length; index++) {
                Type arg = info.args[index];
                of.visitInsn(Opcodes.DUP);
                of.visitConstant(index);
                of.visitVarInsn(arg.getOpcode(Opcodes.ILOAD), argIndex);
                box(of, arg);
                of.visitInsn(Opcodes.AASTORE);
                argIndex += arg.getSize();
            }

            // Call the constructor passing in the generated array
            of.visitMethodInsn(Opcodes.INVOKESPECIAL, ref, Constants.CTOR, CTOR_DESC, false);

            // Return the new object
            of.visitInsn(Opcodes.ARETURN);

            of.visitMaxs(6, Bytecode.getArgsSize(info.args));
            of.visitEnd();
        }

        /**
         * Generate the getter method for each arguments. These getters are not
         * available from consumer code, but instead are called by the injector to
         * retrieve each argument in turn for passing to the method invocation being
         * modified.
         *
         * @param writer Class writer
         */
        private void generateGetters(ArgsClassInfo info, ClassVisitor writer) {
            byte argIndex = 0;
            for (Type arg : info.args) {
                String name = GETTER_PREFIX + argIndex;
                String sig = "()" + arg.getDescriptor();
                MethodVisitorEx get = new MethodVisitorEx(writer.visitMethod(Opcodes.ACC_PUBLIC, name, sig, null, null));
                get.visitCode();

                // Read the value from the values field
                get.visitVarInsn(Opcodes.ALOAD, 0);
                get.visitFieldInsn(Opcodes.GETFIELD, info.getName(), VALUES_FIELD, OBJECT_ARRAY);
                get.visitConstant(argIndex);
                get.visitInsn(Opcodes.AALOAD);

                // Unbox (if primitive) or cast down the value
                unbox(get, arg);

                // Return the value
                get.visitInsn(arg.getOpcode(Opcodes.IRETURN));

                get.visitMaxs(2, 1);
                get.visitEnd();
                argIndex++;
            }
        }

        /**
         * Generate the setter methods. These methods implement the abstract
         * {@link Args#set} and {@link Args#setAll} methods.
         *
         * @param writer Class writer
         */
        private void generateSetters(ArgsClassInfo info, ClassVisitor writer) {
            this.generateIndexedSetter(info, writer);
            this.generateMultiSetter(info, writer);
        }

        /**
         * Generate the <tt>set</tt> method body. The <tt>set</tt> method performs a
         * <tt>CHECKCAST</tt> on all incoming arguments, checks that the argument
         * index is not out of bounds, and also ensures that primitive types are not
         * assigned <tt>null</tt> by the consumer code.
         *
         * @param writer Class writer
         */
        private void generateIndexedSetter(ArgsClassInfo info, ClassVisitor writer) {
            MethodVisitorEx set = new MethodVisitorEx(writer.visitMethod(Opcodes.ACC_PUBLIC,
                    SET, SET_DESC, null, null));
            set.visitCode();

            Label store = new Label(), checkNull = new Label();
            Label[] labels = new Label[info.args.length];
            for (int label = 0; label < labels.length; label++) {
                labels[label] = new Label();
            }

            // Put the values array on the stack to begin with
            set.visitVarInsn(Opcodes.ALOAD, 0);
            set.visitFieldInsn(Opcodes.GETFIELD, info.getName(), VALUES_FIELD, OBJECT_ARRAY);

            // Each argument index will jump to its own label
            for (byte index = 0; index < info.args.length; index++) {
                set.visitVarInsn(Opcodes.ILOAD, 1);
                set.visitConstant(index);
                set.visitJumpInsn(Opcodes.IF_ICMPEQ, labels[index]);
            }

            // No argument was matched, so we throw an out of bounds exception
            throwIOOBE(set, 1);

            // For each arg we do a CHECKCAST to ensure the supplied type is
            // assignable to the arg type, we leave the index and value on the stack
            // and jump to the next stage
            for (int index = 0; index < info.args.length; index++) {
                String boxingType = Bytecode.getBoxingType(info.args[index]);
                set.visitLabel(labels[index]);
                set.visitVarInsn(Opcodes.ILOAD, 1);
                set.visitVarInsn(Opcodes.ALOAD, 2);
                set.visitTypeInsn(Opcodes.CHECKCAST, boxingType != null ? boxingType : info.args[index].getInternalName());
                set.visitJumpInsn(Opcodes.GOTO, boxingType != null ? checkNull : store);
            }

            // For primitive types, we check that the supplied value is not null
            set.visitLabel(checkNull);
            set.visitInsn(Opcodes.DUP);
            set.visitJumpInsn(Opcodes.IFNONNULL, store);

            // If the arg type is primitive but the user supplied NULL, throw an exception
            throwNPE(set, "Argument with primitive type cannot be set to NULL");

            // Everything above succeeded, so we just assign the value into the array
            set.visitLabel(store);
            set.visitInsn(Opcodes.AASTORE);
            set.visitInsn(Opcodes.RETURN);
            set.visitMaxs(6, 3);
            set.visitEnd();
        }

        /**
         * Generate the varargs <tt>set</tt> method body. The <tt>set</tt> method
         * performs a <tt>CHECKCAST</tt> on all incoming arguments, and also ensures
         * that primitive types are not assigned <tt>null</tt> by the consumer code.
         *
         * @param writer Class writer
         */
        private void generateMultiSetter(ArgsClassInfo info, ClassVisitor writer) {
            MethodVisitorEx set = new MethodVisitorEx(writer.visitMethod(Opcodes.ACC_PUBLIC,
                    SETALL, SETALL_DESC, null, null));
            set.visitCode();

            Label lengthOk = new Label(), nullPrimitive = new Label();
            int maxStack = 6;

            // Compare the length of the varargs array to the expected argument count
            set.visitVarInsn(Opcodes.ALOAD, 1);
            set.visitInsn(Opcodes.ARRAYLENGTH);
            set.visitInsn(Opcodes.DUP);
            set.visitConstant((byte)info.args.length);

            // If the lengths are the same, proceed with assignment
            set.visitJumpInsn(Opcodes.IF_ICMPEQ, lengthOk);

            throwIAE(set, info);

            set.visitLabel(lengthOk);
            set.visitInsn(Opcodes.POP); // Pop the remaining length value

            // Put the values array on the stack to begin with
            set.visitVarInsn(Opcodes.ALOAD, 0);
            set.visitFieldInsn(Opcodes.GETFIELD, info.getName(), VALUES_FIELD, OBJECT_ARRAY);

            for (byte index = 0; index < info.args.length; index++) {
                // Dup the member array reference and target index
                set.visitInsn(Opcodes.DUP);
                set.visitConstant(index);

                // Read the value from the varargs array
                set.visitVarInsn(Opcodes.ALOAD, 1);
                set.visitConstant(index);
                set.visitInsn(Opcodes.AALOAD);

                // Check the argument type
                String boxingType = Bytecode.getBoxingType(info.args[index]);
                set.visitTypeInsn(Opcodes.CHECKCAST, boxingType != null ? boxingType : info.args[index].getInternalName());

                // For primitives, check the value is not null
                if (boxingType != null) {
                    set.visitInsn(Opcodes.DUP);
                    set.visitJumpInsn(Opcodes.IFNULL, nullPrimitive);
                    maxStack = 7;
                }

                // Everything succeeded, assign the value
                set.visitInsn(Opcodes.AASTORE);
            }

            set.visitInsn(Opcodes.RETURN);

            set.visitLabel(nullPrimitive);
            throwNPE(set, "Argument with primitive type cannot be set to NULL");
            set.visitInsn(Opcodes.RETURN);

            set.visitMaxs(maxStack, 2);
            set.visitEnd();
        }

        private static void throwIAE(MethodVisitorEx method, ArgsClassInfo info) {
            // Otherwise prepare and throw an ArgumentCountException
            method.visitTypeInsn(Opcodes.NEW, IAE);
            method.visitInsn(DUP);
            method.visitLdcInsn("Expected array of length " + info.args.length + " for Args class with signature: " + info.getSignature());

            method.visitMethodInsn(Opcodes.INVOKESPECIAL, IAE, Constants.CTOR, IAE_CTOR_DESC, false);
            method.visitInsn(Opcodes.ATHROW);
        }

        /**
         * Add insns to throw a null pointer exception with the specified message
         */
        private static void throwNPE(MethodVisitorEx method, String message) {
            method.visitTypeInsn(Opcodes.NEW, NPE);
            method.visitInsn(Opcodes.DUP);
            method.visitLdcInsn(message);
            method.visitMethodInsn(Opcodes.INVOKESPECIAL, NPE, Constants.CTOR, NPE_CTOR_DESC, false);
            method.visitInsn(Opcodes.ATHROW);
        }

        /**
         * Add insns to throw an {@link IndexOutOfBoundsException}, reads
         * the arg index from the local var specified by <tt>arg</tt>
         */
        private static void throwIOOBE(MethodVisitorEx method, int arg) {
            method.visitTypeInsn(Opcodes.NEW, IOOBE);
            method.visitInsn(Opcodes.DUP);
            method.visitVarInsn(Opcodes.ILOAD, arg);
            method.visitMethodInsn(Opcodes.INVOKESPECIAL, IOOBE, Constants.CTOR, IOOBE_CTOR_DESC, false);
            method.visitInsn(Opcodes.ATHROW);
        }

        /**
         * Box (if necessary) the supplied primitive type. Does not affect
         * reference types.
         *
         * @param method method visitor
         * @param var type to box
         */
        private static void box(MethodVisitor method, Type var) {
            String boxingType = Bytecode.getBoxingType(var);
            if (boxingType != null) {
                String desc = String.format("(%s)L%s;", var.getDescriptor(), boxingType);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, boxingType, "valueOf", desc, false);
            }
        }

        /**
         * Unbox (if necessary, otherwise just <tt>CHECKCAST</tt>) the supplied type
         *
         * @param method method visitor
         * @param var type to unbox
         */
        private static void unbox(MethodVisitor method, Type var) {
            String boxingType = Bytecode.getBoxingType(var);
            if (boxingType != null) {
                String unboxingMethod = Bytecode.getUnboxingMethod(var);
                String desc = "()" + var.getDescriptor();
                method.visitTypeInsn(Opcodes.CHECKCAST, boxingType);
                method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, boxingType, unboxingMethod, desc, false);
            } else {
                method.visitTypeInsn(Opcodes.CHECKCAST, var.getInternalName());
            }
        }
    }
}
