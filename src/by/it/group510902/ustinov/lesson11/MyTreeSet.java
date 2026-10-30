package by.it.group510902.ustinov.lesson11;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/*
  Пять свойств красно-чёрного дерева:
    1. Каждый узел либо красный, либо чёрный.
    2. Корень — чёрный.
    3. Все листья (NIL-узлы) — чёрные.
    4. У красного узла оба ребёнка чёрные (двух красных подряд нет).
    5. Все пути от любого узла до листьев содержат одинаковое число чёрных узлов
       (одинаковая чёрная высота).
 */
public class MyTreeSet<E> implements Set<E> {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    // Узел дерева: ключ, ссылки на потомков и родителя, цвет.
    private static final class Node<E> {
        E key;
        Node<E> left;
        Node<E> right;
        Node<E> parent;
        boolean color;

        Node(E key, boolean color) {
            this.key = key;
            this.color = color;
        }
    }

    // Сторожевой узел: играет роль null-листа и всегда остаётся чёрным.
    private final Node<E> nil;
    private Node<E> root;
    private int size;

    public MyTreeSet() {
        nil = new Node<>(null, BLACK);
        nil.left = nil;
        nil.right = nil;
        nil.parent = nil;
        root = nil;
    }

    @Override
    // Обход в порядке возрастания (in-order) даёт отсортированную строку
    public String toString() {
        if (size == 0) {
            return "[]";
        }
        StringBuilder res = new StringBuilder("[");
        Node<E> x = minimum(root);
        while (x != nil) {
            res.append(x.key);
            x = successor(x);
            if (x != nil) {
                res.append(", ");
            }
        }
        return res.append(']').toString();
    }

    @Override
    // Возвращает количество элементов в множестве
    public int size() {
        return size;
    }

    @Override
    // Очищает множество: корень снова указывает на сторожевой узел
    public void clear() {
        root = nil;
        nil.parent = nil;
        size = 0;
    }

    @Override
    // Проверяет, пусто ли множество
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    // Вставляет элемент за O(log n): спуск по дереву, вставка красного узла
    // и восстановление свойств красно-чёрного дерева в insertFixup
    public boolean add(E e) {
        Node<E> parent = nil;
        Node<E> current = root;
        int cmp = 0;

        while (current != nil) {
            parent = current;
            cmp = compare(e, current.key);
            if (cmp == 0) {
                return false;                       // такой элемент уже есть
            }
            current = (cmp < 0) ? current.left : current.right;
        }

        Node<E> node = new Node<>(e, RED);
        node.left = nil;
        node.right = nil;
        node.parent = parent;

        if (parent == nil) {
            root = node;                            // дерево было пустым
        } else if (cmp < 0) {
            parent.left = node;
        } else {
            parent.right = node;
        }

        insertFixup(node);
        size++;
        return true;
    }

    @Override
    // Удаляет элемент за O(log n): поиск узла и удаление с балансировкой
    public boolean remove(Object o) {
        Node<E> node = findNode(o);
        if (node == nil) {
            return false;
        }
        deleteNode(node);
        size--;
        return true;
    }

    @Override
    // Поиск элемента за O(log n)
    public boolean contains(Object o) {
        return findNode(o) != nil;
    }

    @Override
    // Проверяет, что все элементы коллекции c есть в множестве
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    // Добавляет все элементы коллекции c; true — если множество изменилось
    public boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E e : c) {
            if (add(e)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    // Удаляет элементы, присутствующие в c; true — если что-то удалено
    public boolean removeAll(Collection<?> c) {
        if (c.isEmpty() || isEmpty()) {
            return false;
        }
        boolean changed = false;
        for (Object key : keysInOrder()) {
            if (c.contains(key)) {
                remove(key);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    // Оставляет только элементы, присутствующие в c; true — если что-то удалено
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for (Object key : keysInOrder()) {
            if (!c.contains(key)) {
                remove(key);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    /////////////////////////////////////////////////////////////////////////
    //                    Внутреннее устройство дерева                     //
    /////////////////////////////////////////////////////////////////////////

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int compare(Object a, Object b) {
        return ((Comparable) a).compareTo(b);
    }

    private Node<E> findNode(Object key) {
        Node<E> current = root;
        while (current != nil) {
            int cmp = compare(key, current.key);
            if (cmp == 0) {
                return current;
            }
            current = (cmp < 0) ? current.left : current.right;
        }
        return nil;
    }

    private Node<E> minimum(Node<E> x) {
        while (x.left != nil) {
            x = x.left;
        }
        return x;
    }

    /** Следующий по возрастанию узел (для in-order обхода). */
    private Node<E> successor(Node<E> x) {
        if (x.right != nil) {
            x = x.right;
            while (x.left != nil) {
                x = x.left;
            }
            return x;
        }
        Node<E> y = x.parent;
        while (y != nil && x == y.right) {
            x = y;
            y = y.parent;
        }
        return y;
    }

    /** Элементы в порядке возрастания — внутренний обход (не публичный toArray). */
    private Object[] keysInOrder() {
        Object[] result = new Object[size];
        int i = 0;
        for (Node<E> x = minimum(root); x != nil; x = successor(x)) {
            result[i++] = x.key;
        }
        return result;
    }

    /** Левый поворот вокруг узла x. */
    private void rotateLeft(Node<E> x) {
        Node<E> y = x.right;
        x.right = y.left;
        if (y.left != nil) {
            y.left.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == nil) {
            root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }
        y.left = x;
        x.parent = y;
    }

    /** Правый поворот вокруг узла x. */
    private void rotateRight(Node<E> x) {
        Node<E> y = x.left;
        x.left = y.right;
        if (y.right != nil) {
            y.right.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == nil) {
            root = y;
        } else if (x == x.parent.right) {
            x.parent.right = y;
        } else {
            x.parent.left = y;
        }
        y.right = x;
        x.parent = y;
    }

    /** Восстановление свойств после вставки красного узла z. */
    private void insertFixup(Node<E> z) {
        while (z.parent.color == RED) {
            if (z.parent == z.parent.parent.left) {
                Node<E> uncle = z.parent.parent.right;
                if (uncle.color == RED) {
                    // случай 1: красный «дядя» — перекрашиваем и идём выше
                    z.parent.color = BLACK;
                    uncle.color = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.right) {
                        // случай 2: «зигзаг» — превращаем в «молнию»
                        z = z.parent;
                        rotateLeft(z);
                    }
                    // случай 3: «молния» — перекраска и поворот
                    z.parent.color = BLACK;
                    z.parent.parent.color = RED;
                    rotateRight(z.parent.parent);
                }
            } else {
                Node<E> uncle = z.parent.parent.left;
                if (uncle.color == RED) {
                    z.parent.color = BLACK;
                    uncle.color = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.left) {
                        z = z.parent;
                        rotateRight(z);
                    }
                    z.parent.color = BLACK;
                    z.parent.parent.color = RED;
                    rotateLeft(z.parent.parent);
                }
            }
        }
        root.color = BLACK;
    }

    /** Переставляет поддерево v на место поддерева u. */
    private void transplant(Node<E> u, Node<E> v) {
        if (u.parent == nil) {
            root = v;
        } else if (u == u.parent.left) {
            u.parent.left = v;
        } else {
            u.parent.right = v;
        }
        v.parent = u.parent;
    }

    /** Удаление узла z с балансировкой дерева. */
    private void deleteNode(Node<E> z) {
        Node<E> removed = z;
        boolean removedColor = removed.color;
        Node<E> x;

        if (z.left == nil) {
            x = z.right;
            transplant(z, z.right);
        } else if (z.right == nil) {
            x = z.left;
            transplant(z, z.left);
        } else {
            removed = minimum(z.right);
            removedColor = removed.color;
            x = removed.right;
            if (removed.parent == z) {
                x.parent = removed;
            } else {
                transplant(removed, removed.right);
                removed.right = z.right;
                removed.right.parent = removed;
            }
            transplant(z, removed);
            removed.left = z.left;
            removed.left.parent = removed;
            removed.color = z.color;
        }

        if (removedColor == BLACK) {
            deleteFixup(x);
        }
    }

    /** Восстановление свойств после удаления чёрного узла (x — «дважды чёрный»). */
    private void deleteFixup(Node<E> x) {
        while (x != root && x.color == BLACK) {
            if (x == x.parent.left) {
                Node<E> brother = x.parent.right;
                if (brother.color == RED) {
                    // случай 1: красный брат — поворот, чтобы брат стал чёрным
                    brother.color = BLACK;
                    x.parent.color = RED;
                    rotateLeft(x.parent);
                    brother = x.parent.right;
                }
                if (brother.left.color == BLACK && brother.right.color == BLACK) {
                    // случай 2: оба ребёнка брата чёрные — поднимаемся выше
                    if (brother != nil) {
                        brother.color = RED;
                    }
                    x = x.parent;
                } else {
                    if (brother.right.color == BLACK) {
                        // случай 3: преобразуем в случай 4
                        brother.left.color = BLACK;
                        brother.color = RED;
                        rotateRight(brother);
                        brother = x.parent.right;
                    }
                    // случай 4: завершающий поворот
                    brother.color = x.parent.color;
                    x.parent.color = BLACK;
                    brother.right.color = BLACK;
                    rotateLeft(x.parent);
                    x = root;
                }
            } else {
                Node<E> brother = x.parent.left;
                if (brother.color == RED) {
                    brother.color = BLACK;
                    x.parent.color = RED;
                    rotateRight(x.parent);
                    brother = x.parent.left;
                }
                if (brother.right.color == BLACK && brother.left.color == BLACK) {
                    if (brother != nil) {
                        brother.color = RED;
                    }
                    x = x.parent;
                } else {
                    if (brother.left.color == BLACK) {
                        brother.right.color = BLACK;
                        brother.color = RED;
                        rotateLeft(brother);
                        brother = x.parent.left;
                    }
                    brother.color = x.parent.color;
                    x.parent.color = BLACK;
                    brother.left.color = BLACK;
                    rotateRight(x.parent);
                    x = root;
                }
            }
        }
        x.color = BLACK;
        nil.color = BLACK;
    }
}
