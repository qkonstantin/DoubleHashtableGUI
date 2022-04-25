package org.example;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class HashTable<K, V> implements Map<K, V> {

    private final int HASH_CONST = 47;

    private int size = 0;
    private int capacity = 16;
    private boolean[] deletedCells;
    private float loadFactor = 0.75f;
    private Cell<K,V>[] table;


    /* Создает новую пустую хэш-таблицу с заданной начальной емкостью и заданным коэффициентом загрузки.
     * Параметры:
     * capacity – начальная емкость хэш-таблицы.
     * loadFactor – коэффициент загрузки хэш-таблицы.
     * Бросает:
     * Исключение IllegalArgumentException – если начальная емкость меньше нуля
     * или если коэффициент загрузки является неположительным.
     */
    public HashTable(int capacity, float loadFactor) {
        if (capacity < 0) {
            throw new IllegalArgumentException(
                    "Illegal Capacity: " + capacity
            );
        }

        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal Load: " + loadFactor);
        }

        if (capacity==0) {
            capacity = 1;
        }

        this.loadFactor = loadFactor;
        table = new Cell[capacity];
        deletedCells = new boolean[capacity];
    }

    /* Создает новую пустую хэш-таблицу с заданной начальной емкостью и коэффициентом загрузки по умолчанию (0,75).
     * Параметры:
     * capacity – начальная емкость хэш-таблицы.
     * Бросает:
     * Исключение IllegalArgumentException – если начальная емкость меньше нуля.
     */
    public HashTable(int capacity) {
        this(capacity, 0.75f);
    }

    // Создает новую пустую хэш-таблицу с начальной емкостью по умолчанию (16) и коэффициентом загрузки (0,75).
    public HashTable() {
        this(16, 0.75f);
    }

    // Возвращает количество ключей в этой хэш-таблице.
    @Override
    public int size() {
        return size;
    }

    /* Проверяет, не сопоставляет ли эта хэш-таблица ключи со значениями.
     * Возвращается:
     * true, если эта хэш-таблица не сопоставляет ключи со значениями; false в противном случае.
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    // Проверяет, является ли указанный объект ключом в этой хэш-таблице.
    @Override
    public boolean containsKey(Object key) {
        return contains(key) >= 0;
    }

    // Возвращает значение true, если эта хэш-таблица сопоставляет один или несколько ключей с этим значением.
    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException("HashTable does not permit null values");
        }

        for (int i = 0; i < capacity; i++) {
            if (!deletedCells[i] && table[i] != null) {
                if (value.equals(table[i].getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    // Возвращает значение, которому сопоставлен указанный ключ, или значение null,
    // если эта map не содержит сопоставления для ключа.
    @Override
    public V get(Object key) {
        int index = contains(key);
        if (index < 0) {
            return null;
        }
        return table[index].getValue();
    }

    // Сопоставляет указанный ключ с указанным значением в этой хэш-таблице.
    // Ни ключ, ни значение не могут быть нулевыми.
    @Override
    public V put(K key, V value) {
        if (value == null || key == null) {
            throw new NullPointerException();
        }

        if (contains(key) >= 0) {
            int index = this.contains(key);
            V oldValue = table[index].getValue();
            table[index].setValue(value);
            return oldValue;
        }

        size++;
        double newLoadFactor = (double) size / capacity;
        if (newLoadFactor >= loadFactor) {
            rehash();
        }

        int index = findEmptyIndex(key);
        Cell<K,V> newCell = new Cell<>(key, value);
        table[index] = newCell;

//      Returns null if there was no mapping for key before
        return null;
    }


    // Удаляет ключ (и соответствующее ему значение) из этой хэш-таблицы.
    // Этот метод ничего не делает, если ключа нет в хэш-таблице.
    @Override
    public V remove(Object key) {
        int index = contains(key);

        if (index < 0) {
            return null;
        }

        V deletedValue = table[index].getValue();

        size--;
        deletedCells[index] = true;
        return deletedValue;
    }

    /* Копирует все сопоставления из указанной map в эту хэш-таблицу.
     * Эти сопоставления заменят любые сопоставления,
     * которые эта хэш-таблица имела для любого из ключей,
     * находящихся в данный момент в указанной map.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> elem : map.entrySet()) {
            put(elem.getKey(), elem.getValue());
        }
    }

    // Очищает эту хэш-таблицу, чтобы она не содержала ключей.
    @Override
    public void clear() {
        for (int i = 0; i < capacity; i++) {
            table[i] = null;
        }
        size = 0;
        deletedCells = new boolean[capacity];
    }

    private Set<K> keySet;
    private Set<Map.Entry<K,V>> entrySet;
    private Collection<V> values;

    private static final int KEYS = 0;
    private static final int VALUES = 1;
    private static final int ENTRIES = 2;

    /* Возвращает заданный вид ключей, содержащихся в этой map.
     * Набор поддерживается map, поэтому изменения в map отражаются в наборе, и наоборот.
     * Если map изменяется во время выполнения итерации по набору
     * (за исключением собственной операции удаления итератора), результаты итерации не определены.
     */
    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new KeySet();
        }
        return keySet;
    }

    /* Набор поддерживает удаление элемента,
     * которое удаляет соответствующее отображение из map с помощью операций
     * Iterator.remove, Set.remove, removeAll, retainAll и clear.
     */
    private class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return getIterator(KEYS);
        }

        public int size() {
            return size;
        }

        public boolean contains(Object o) {
            return containsKey(o);
        }

        public boolean remove(Object o) {
            return HashTable.this.remove(o) != null;
        }

        public void clear() {
            HashTable.this.clear();
        }
    }

    /* Возвращает представление коллекции значений, содержащихся в этой map.
     * Коллекция поддерживается map, поэтому изменения, внесенные в map, отражаются в коллекции, и наоборот.
     * Если map изменяется во время выполнения итерации по коллекции
     * (за исключением собственной операции удаления итератора), результаты итерации не определены.
     */
    @Override
    public Collection<V> values() {
        if (values == null) {
            values = new ValueCollection();
        }
        return values;
    }

    /* Коллекция поддерживает удаление элементов,
     * которое удаляет соответствующее отображение из map с помощью операций
     * Iterator.remove, Collection.remove, removeAll, retainAll и clear.
     */
    private class ValueCollection extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return getIterator(VALUES);
        }

        public int size() {
            return size;
        }

        public boolean contains(Object o) {
            return containsValue(o);
        }

        public void clear() {
            HashTable.this.clear();
        }
    }

    /* Возвращает заданный вид отображений, содержащихся в этой map.
     * Набор поддерживается map, поэтому изменения в map отражаются в наборе, и наоборот.
     * Если map изменяется во время выполнения итерации по набору
     * (за исключением собственной операции удаления итератора), результаты итерации не определены.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    /* Набор поддерживает удаление элемента,
     * которое удаляет соответствующее отображение из map с помощью операций
     * Iterator.remove, Set.remove, removeAll, retainAll и clear.
     */
    class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        public Iterator<Entry<K, V>> iterator() {
            return getIterator(ENTRIES);
        }

        public boolean add(Map.Entry<K, V> o) {
            return super.add(o);
        }

        public boolean contains(Object o) {
            if (!(o instanceof Cell)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            Object key = entry.getKey();
            return HashTable.this.containsKey(key);
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            Object key = entry.getKey();
            return HashTable.this.remove(key) != null;
        }

        public void clear() {
            HashTable.this.clear();
        }

        @Override
        public int size() {
            return size;
        }
    }

    // Сравнивает указанный объект с этой map на предмет равенства в соответствии с определением в интерфейсе map.
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Map)) {
            return false;
        }

        Map<?,?> t = (Map<?,?>) o;
        if (t.size() != size())
            return false;

        try {
            for (Map.Entry<K, V> e : entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                if (!value.equals(t.get(key))) {
                    return false;
                }
            }
        } catch (ClassCastException | NullPointerException ex)   {
            return false;
        }

        return true;
    }

    // Возвращает значение хэш-кода для этой map в соответствии с определением в интерфейсе map.
    @Override
    public int hashCode() {
        int hash = 0;
        for (Entry<K, V> entry : entrySet) {
            hash += entry.hashCode();
        }
        return hash;
    }

    // Возвращает строковое представление содержимого указанного массива.
    @Override
    public String toString() {
        return Arrays.toString(table);
    }

    // Возвращает значение, которому сопоставлен указанный ключ, или значение по умолчанию,
    // если эта map не содержит сопоставления для ключа.
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        int index = contains(key);
        if (index >= 0) {
            return table[index].getValue();
        } else {
            return defaultValue;
        }
    }

    // Выполняет указанное действие для каждой записи в этой map до тех пор,
    // пока все записи не будут обработаны или действие не вызовет исключение.
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);

        if (entrySet == null) {
            entrySet = entrySet();
        }

        int size = entrySet.size();

        for (Entry<K, V> cell : entrySet) {
            action.accept(cell.getKey(), cell.getValue());
            if (entrySet.size() != size) {
                throw new ConcurrentModificationException();
            }
        }
    }

    // Заменяет значение каждой записи результатом вызова данной функции для этой записи до тех пор,
    // пока все записи не будут обработаны или функция не выдаст исключение.
    @Override
    public synchronized void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);

        if (entrySet == null) {
            entrySet = entrySet();
        }

        int size = entrySet.size();

        for (Entry<K, V> cell : entrySet) {
            Objects.requireNonNull(
                    cell.setValue(function.apply(cell.getKey(), cell.getValue()))
            );
            if (entrySet.size() != size) {
                throw new ConcurrentModificationException();
            }
        }
    }

    // Если указанный ключ еще не связан со значением (или сопоставлен нулю),
    // он связывает его с заданным значением и возвращает null, иначе возвращает текущее значение.
    @Override
    public V putIfAbsent(K key, V value) {
        int index = contains(key);
        if (index >= 0) {
            return table[index].getValue();
        } else {
            return put(key, value);
        }
    }

    // Удаляет запись для указанного ключа только в том случае,
    // если в данный момент она сопоставлена с указанным значением.
    @Override
    public boolean remove(Object key, Object value) {
        V curValue = get(key);

        if (!Objects.equals(curValue, value)) {
            return false;
        }

        remove(key);
        return true;
    }

    // Заменяет запись для указанного ключа только в том случае,
    // если в данный момент сопоставлено с указанным значением.
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        if (oldValue == null) throw new NullPointerException();

        int index = contains(key);
        if (index < 0 || !table[index].getValue().equals(oldValue)) {
            return false;
        }

        table[index].setValue(newValue);
        return true;
    }

    // Заменяет запись для указанного ключа только в том случае,
    // если в данный момент он сопоставлен с некоторым значением.
    @Override
    public V replace(K key, V value) {
        int index = contains(key);
        if (index < 0) {
            return null;
        }

        V old = table[index].getValue();
        table[index].setValue(value);
        return old;
    }

    /* Если указанный ключ еще не связан со значением (или сопоставлен нулю),
     * пытается вычислить его значение с помощью заданной функции сопоставления и вводит его в эту map,
     * если не указано значение null.
     * Если функция сопоставления возвращает значение null,
     * сопоставление не записывается. Если функция сопоставления сама выдает (непроверенное) исключение,
     * исключение повторно генерируется, и сопоставление не записывается.
     */
    @Override
    public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);

        int index = contains(key);
        if (index >= 0) {
            return table[index].getValue();
        } else {
            V newValue = mappingFunction.apply(key);

            if (newValue == null) {
                throw new NullPointerException("Computed value is null");
            }

            put(key, newValue);
            return newValue;
        }
    }

    /* Если значение для указанного ключа присутствует и не равно нулю,
     * выполняется попытка вычисления нового сопоставления с учетом ключа и его текущего сопоставленного значения.
     * Если функция переназначения возвращает значение null, сопоставление удаляется.
     * Если сама функция переназначения выдает (непроверенное) исключение, исключение повторно генерируется,
     * а текущее сопоставление остается неизменным.
     */
    @Override
    public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        int index = contains(key);
        if (index >= 0) {
            Cell<K,V> cell = table[index];
            V newValue = remappingFunction.apply(key, cell.getValue());

            if (newValue == null) {
                throw new NullPointerException("Computed value is null");
            }

            cell.setValue(newValue);
            return newValue;
        }

        return null;
    }

    // Пытается вычислить сопоставление для указанного ключа и его текущего сопоставленного значения
    // (или null, если текущее сопоставление отсутствует).
    @Override
    public synchronized V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        int index = contains(key);

        if (index >= 0) {
            V oldValue = table[index].getValue();
            V newValue = remappingFunction.apply(key, oldValue);

            if (newValue != null) {
                put(key, newValue);
                return newValue;
            } else {
                remove(key);
                return null;
            }
        } else {
            V newValue = remappingFunction.apply(key, null);
            return put(key, newValue);
        }
    }


    /* Если указанный ключ еще не связан со значением или связан с null,
     * связывает его с заданным ненулевым значением.
     * В противном случае заменяет связанное значение результатами данной функции переназначения или удаляет,
     * если результат равен нулю.
     * Этот метод может быть полезен при объединении нескольких сопоставленных значений для ключа.
     */
    @Override
    public synchronized V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        int index = contains(key);

        if (index >= 0) {
            Cell<K,V> cell = table[index];
            V newValue = remappingFunction.apply(cell.getValue(), value);
            if (newValue != null) {
                cell.setValue(newValue);
                return newValue;
            } else {
                remove(key);
                return null;
            }
        } else {
            put(key, value);
            return value;
        }
    }

    // Возвращает первое хэш-значение.
    private int hash1(Object key) {
        return (key.hashCode() * HASH_CONST) % capacity;
    }

    // Возвращает второе хэш-значение.
    private int hash2(Object key) {
        int hash = (key.hashCode() * HASH_CONST) % (capacity - 1);
        if (hash % 2 == 0) {
            hash++;
        }
        return hash;
    }

    /* Используется для увеличения емкости хэш-таблицы.
     * Этот метод вызывается автоматически,
     * когда количество ключей в хэш-таблице превышает емкость и коэффициент загрузки этой хэш-таблицы.
     * Он также внутренне реорганизует эту хэш-таблицу.
     */
    private void rehash() {
        int oldCapacity = capacity;
        capacity = capacity * 2 + 1;

        Cell<K, V>[] subTable = Arrays.copyOf(table, oldCapacity);
        table = new Cell[capacity];
        deletedCells = new boolean[capacity];

        for (int i = 0; i < oldCapacity; i++) {
            if (subTable[i] != null) {
                int index = findEmptyIndex(subTable[i].getKey());
                table[index] = subTable[i];
            }
        }
    }

    // Возвращает пустой индекс при поиске с начала или конца.
    private int findEmptyIndex(K key) {
        int n = -1;
        while (true) {
            n++;
            int index = (hash1(key) + n * hash2(key)) % (capacity - 3);
            if (index < 0) {
                index = index * -1;
            }
            if (table[index] == null) {
                return index;
            } else if (deletedCells[index]){
                deletedCells[index] = false;
                return  index;
            }
        }
    }

    // Проверка на содержание объектов.
    private int contains(Object key) {
        if (key == null) throw new NullPointerException("HashTable does not permit null keys");
        int hash1 = hash1(key);
        int hash2 = hash2(key);
        int n = -1;
        while (n != capacity - 1) {
            n++;
            int index = (hash1 + n * hash2) % (capacity - 3);
            if (index < 0) {
                index = index * -1;
            }
            Cell<K, V> cell = table[index];
            if (cell != null && cell.getKey().equals(key)) {
                if (deletedCells[index]) {
                    return -1;
                } else {
                    return index;
                }
            }
        }
        return -1;
    }

    // Возвращает итератор, который выполняет итерацию по всем элементам.
    private <T> Iterator<T> getIterator(int type) {
        if (size == 0) {
            return Collections.emptyIterator();
        } else {
            return new HashIterator<T>(type);
        }
    }

    // Позволяет выполнять итерации по набору значений в хэш-таблице.
    class HashIterator<T> implements Iterator<T> {
        boolean[] iterDeletedCells = deletedCells;
        int index = -1;
        int count = 0;
        int iterSize = size;
        int type;

        HashIterator(int type) {
            this.type = type;
        }

        // Проверка, есть ли следующий элемент, и не достигнут ли конец коллекции.
        @Override
        public boolean hasNext() {
            return count < iterSize;
        }

        // Получение следующующего элемента.
        @Override
        public T next() {
            index++;
            while (index < capacity) {
                Cell<K,V> cell = table[index];
                if (!iterDeletedCells[index] && cell != null) {
                    count++;
                    return type == KEYS ? (T) cell.getKey() : (type == VALUES ? (T) cell.getValue() : (T) cell);
                } else {
                    index++;
                }
            }
            return null;
        }
    }

    // Запись списка коллизий хэш-таблицы.
    private static class Cell<K, V> implements Map.Entry<K, V> {

        private K key;
        private V value;

        public Cell(K key, V value) {
            this.key = key;
            this.value = value;
        }

        // Получение ключа.
        @Override
        public K getKey() {
            return key;
        }

        // Получение значения
        @Override
        public V getValue() {
            return value;
        }

        // Установка значения.
        @Override
        public V setValue(V value) {
            this.value = value;
            return value;
        }

        // Проверка на содержание.
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cell<?, ?> cell = (Cell<?, ?>) o;
            return Objects.equals(key, cell.key) &&
                    Objects.equals(value, cell.value);
        }

        // Возвращение хэш-значения.
        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        // Преобразование в строку.
        @Override
        public String toString() {
            return key +
                    "=" + value;
        }
    }

}
