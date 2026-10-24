package com.airdata.maintenance.ml;

import java.io.*;

/**
 * Handles persistence and deserialization of trained ML model artifacts.
 */
public class ModelSerializer {

    public static void saveModel(Object model, File targetFile) throws IOException {
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(targetFile)))) {
            oos.writeObject(model);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T loadModel(File sourceFile, Class<T> clazz) throws IOException, ClassNotFoundException {
        if (!sourceFile.exists()) {
            throw new FileNotFoundException("Model file does not exist: " + sourceFile.getAbsolutePath());
        }
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(sourceFile)))) {
            Object obj = ois.readObject();
            return clazz.cast(obj);
        }
    }
}
