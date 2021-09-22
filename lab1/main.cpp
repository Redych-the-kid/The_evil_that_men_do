#include <iostream>
#include <algorithm>
#include <string>
#include <gtest/gtest.h>

using namespace std;

typedef string Key;

struct Value {
    unsigned age;
    unsigned weight;
};

class FlatMap
{
private:
    size_t size;
    size_t capacity;
    Value *val;
    Key *keys;
    size_t bin_util(const Key key) const{
        if(capacity == 0){
            return 0;
        }
        if(key == keys[0])
            return 0;
        if(key == keys[capacity - 1])
            return capacity - 1;
        size_t left = 0,right = capacity - 1;
        while(left < right){
            size_t mid = left + ((right - left) / 2);
            if(key == keys[mid]){
                return mid;
            }
            else if(key > keys[mid]){
                left = mid + 1;
            }
            else{
                right = mid;
            }
        }
        return left;
    }
    int bin_search(const Key key) const{
        size_t id= bin_util(key);
        if(keys[id] == key){
            return id;
        }
        return -1;
    }
    void resize() {
        size_t new_size = size * 2;
        Value * new_val = new Value[new_size];
        Key * new_keys = new Key[new_size];
        copy(val, val + capacity, new_val);
        copy(keys, keys + capacity, new_keys);
        delete[] val;
        delete[] keys;
        val = new_val;
        keys = new_keys;
        size = new_size;
    }
    void set(long long i, const Key& key, const Value& value){
        val[i] = value;
        keys[i] = key;
    }
public:
    FlatMap(){
        size = 1;
        capacity = 0;
        val = new Value[size];
        keys = new Key[size];
    }
    ~FlatMap(){
        delete[] val;;
        delete[] keys;
    }
    FlatMap(const FlatMap& b){
        size = b.size;
        capacity = b.capacity;
        val = new Value[size];
        keys = new Key[size];
        copy(b.val,b.val + capacity, val);
        copy(b.keys, b.keys + capacity, keys);
    }

    FlatMap& operator=(const FlatMap& b){
        if(&b == this){
            return *this;
        }
        size = b.size;
        capacity = b.capacity;
        delete[] val;
        delete[] keys;
        val = new Value[size];
        keys = new Key [size];
        copy(b.val, b.val + capacity, val);
        copy(b.keys, b.keys + capacity, keys);
        return *this;
    }

    // Удаляет элемент по заданному ключу.
    bool erase(const Key& k){
        int id = bin_search(k);
        if(id == -1){
            return false;
        }
        capacity--;
        copy(val + id + 1, val + capacity, val + id);
        copy(keys + id + 1, keys + capacity, keys + id);
    }
    // Вставка в контейнер. Возвращаемое значение - успешность вставки.
    bool contains(const Key& key) const{
        return bin_search(key) != -1;
    }
    bool insert(const Key& key, const Value& value){
        if(contains(key) == true){
            return false;
        }
        if(capacity == size){
            resize();
        }
        if(capacity == 0){
            set(0, key, value);
            capacity++;
            return true;
        }
        if(key > keys[capacity - 1]){
            set(capacity, key, value);
            capacity++;
            return true;
        }
        if(key == keys[0]){
            set(0, key, value);
            return true;
        }
        if(key == keys[capacity - 1]){
            set(capacity - 1, key, value);
            return true;
        }
        size_t id = bin_util(key);
        for(size_t i = capacity;i > id;--i){
            val[i] = val[i - 1];
            keys[i] = keys[i - 1];
        }
        set(id, key, value);
        capacity++;
        return true;
    }

    size_t get_size() const{
        return size;
    }
    bool empty() const{
        return capacity == 0;
    }
    //for debug purposes
    void print(){
        for(size_t i = 0;i < capacity;++i){
            cout << i << "." << keys[i] << ":" << val[i].age << "," << val[i].weight << endl;
        }
    }
};
TEST(TestMap, Empty){
    FlatMap a;
    EXPECT_EQ(true, a.empty());
    a.insert("Rei", {18, 64});
    EXPECT_EQ(false, a.empty());
}
TEST(TestMap, Insert){
    FlatMap a;
    EXPECT_EQ(true, a.insert("Rei",{14, 50}));
    EXPECT_EQ(false, a.insert("Rei",{15, 50}));
    EXPECT_EQ(1, a.get_size());
    EXPECT_EQ(true, a.insert("Asuka", {14, 100}));
}
int main() {
    testing::InitGoogleTest();
    return RUN_ALL_TESTS();
}
