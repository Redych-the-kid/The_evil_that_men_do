#ifndef LAB1_FLATMAP_H
#define LAB1_FLATMAP_H

#include <string>

typedef std::string Key;

struct Value {
    unsigned age;
    unsigned weight;
};

class FlatMap {
public:
    FlatMap();
    ~FlatMap();
    FlatMap(const FlatMap &b);

    FlatMap(FlatMap &&b);
    // Обменивает значения двух флетмап.
    // Подумайте, зачем нужен этот метод, при наличии стандартной функции
    // std::swap.
    void swap(FlatMap &b);

    FlatMap &operator=(const FlatMap &b);

    //FlatMap& operator=(FlatMap&& b);
    // Очищает контейнер.
    void clear();


    // Удаляет элемент по заданному ключу.
    bool erase(const Key &k);
    // Вставка в контейнер. Возвращаемое значение - успешность вставки.
    bool insert(const Key &key, const Value &value);
    // Проверка наличия значения по заданному ключу.
    bool contains(const Key &key) const;

    // Возвращает значение по ключу. Небезопасный метод.
    // В случае отсутствия ключа в контейнере, следует вставить в контейнер
    // значение, созданное конструктором по умолчанию и вернуть ссылку на него.
    Value &operator[](const Key &k);

    // Возвращает значение по ключу. Бросает исключение при неудаче.
    Value &at(const Key &k);

    const Value &at(const Key &k) const;
    size_t get_size() const;

    bool empty() const;
    void print();
    friend bool operator==(const FlatMap &a, const FlatMap &b);

    friend bool operator!=(const FlatMap &a, const FlatMap &b);
    friend bool operator==(Value &a, Value &b);
    friend bool operator==(const Value &a, const Value &b);

private:
    size_t size;
    size_t capacity;
    Value *val = nullptr;
    Key *keys = nullptr;
    size_t bin_util(const Key &key) const;
    size_t bin_search(const Key &key) const;
    void resize();
    void set(size_t i, const Key &key, const Value &value);
};

#endif//LAB1_FLATMAP_H
