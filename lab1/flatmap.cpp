#include "flatmap.h"

FlatMap::FlatMap() {
    capacity = 0;
    size = 1;
    val = new Value[size];
    keys = new Key[size];
}

FlatMap::~FlatMap(){
    delete[] val;
    delete[] keys;
}

FlatMap::FlatMap(const FlatMap& b){
    size = b.size;
    capacity = b.capacity;
    val = new Value[size];
    keys = new Key[size];
    std::copy(b.val,b.val + capacity, val);
    std::copy(b.keys, b.keys + capacity, keys);
}

FlatMap& FlatMap::operator=(const FlatMap& b){
    if(&b == this){
        return *this;
    }
    size = b.size;
    capacity = b.capacity;
    delete[] val;
    delete[] keys;
    val = new Value[size];
    keys = new Key [size];
    std::copy(b.val, b.val + capacity, val);
    std::copy(b.keys, b.keys + capacity, keys);
    return *this;
}

bool FlatMap::erase(const Key &k) {
    int id = bin_search(k);
    if(id == -1){
        return false;
    }
    capacity--;
    for(size_t i = id;i < capacity;i++){
        val[i] = val[i + 1];
        keys[i] = keys[i + 1];
    }
    return true;
}

bool FlatMap::contains(const Key &key) const {
    if(capacity == 0){
        return false;
    }
    return bin_search(key) != -1;
}

bool FlatMap::insert(const Key& key, const Value& value){
    if(contains(key)){
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

size_t FlatMap::get_size() const{
    return capacity;
}

bool FlatMap::empty() const{
    return capacity == 0;
}

void FlatMap::print(){
    for(size_t i = 0;i < capacity;++i){
        std::cout << i << "." << keys[i] << ":" << val[i].age << "," << val[i].weight << std::endl;
    }
}

Value& FlatMap::operator[](const Key& k){
    int el = bin_search(k);
    if(el != -1){
        return val[el];
    }
    auto * v = new Value();
    insert(k, *v);
    return *v;
}

void FlatMap::set(long long i, const Key &key, const Value &value) {
    val[i] = value;
    keys[i] = key;
}

void FlatMap::resize() {
    size_t new_size = size * 2;
    auto * new_val = new Value[new_size];
    Key * new_keys = new Key[new_size];
    std::copy(val, val + capacity, new_val);
    std::copy(keys, keys + capacity, new_keys);
    delete[] val;
    delete[] keys;
    val = new_val;
    keys = new_keys;
    size = new_size;
}

int FlatMap::bin_search(const Key& key) const{
    size_t id= bin_util(key);
    if(keys[id] == key){
        return id;
    }
    return -1;
}

size_t FlatMap::bin_util(const Key& key) const{
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

FlatMap::FlatMap(FlatMap && b) {
    keys = b.keys;
    val = b.val;
    capacity = b.capacity;
    size = b.size;
    b.keys = nullptr;
    b.val = nullptr;
}

void FlatMap::swap(FlatMap& b){
    std::swap(*this, b);
}

void FlatMap::clear(){
    capacity = 0;
}

bool operator == (const Value & a, const Value & b) {
    return  a.age == b.age && a.weight == b.weight;
}

bool operator == (const FlatMap &a, const FlatMap &b) {
    if (a.capacity == b.capacity) {
        for (size_t i = 0; i < a.capacity; ++i) {
            if (!((a.val[i] == b.val[i]) && (a.keys[i] == b.keys[i]))) {
                return false;
            }
        }
    } else {
        return false;
    }
    return true;
}

bool operator != (const FlatMap &a, const FlatMap &b) {
    return !(a == b);
}

Value& FlatMap::at(const Key& k){
    if(capacity == 0){
        throw std::invalid_argument ("Container is empty");
    }
    int el = bin_search(k);
    if(-1 == el){
        throw std::invalid_argument ("Element not found");
    }
    return val[el];
}

const Value& FlatMap::at(const Key& k) const{
    if(capacity == 0){
        throw std::invalid_argument ("Container is empty");
    }
    int el = bin_search(k);
    if(-1 == el){
        throw std::invalid_argument ("Element not found");
    }
    return val[el];
}