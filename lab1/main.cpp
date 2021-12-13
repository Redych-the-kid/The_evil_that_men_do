#include "flatmap.h"
#include <algorithm>
#include <gtest/gtest.h>

using namespace std;

class F_test : public testing::Test {
protected:
    FlatMap a, b;
};

TEST_F(F_test, Empty) {
    EXPECT_TRUE(a.empty());
    a.insert("Rei", {18, 64});
    EXPECT_FALSE(a.empty());
}

TEST_F(F_test, Insert) {
    EXPECT_TRUE(a.insert("Rei", {14, 50}));
    EXPECT_FALSE(a.insert("Rei", {15, 50}));
    EXPECT_EQ(1, a.get_size());
    EXPECT_TRUE(a.insert("Asuka", {14, 100}));
}

TEST_F(F_test, Erase) {
    EXPECT_FALSE(a.erase("EVA00"));
    a["EVA00"] = {1, 2};
    a["EVA01"] = {3, 4};
    a["EVA02"] = {5, 6};
    EXPECT_TRUE(a.erase("EVA00"));
    EXPECT_TRUE(a.erase("EVA01"));
    EXPECT_TRUE(a.erase("EVA02"));
    EXPECT_EQ(0, a.get_size());
}

TEST_F(F_test, Inequality) {
    a.insert("K-ON!", {3, 2});
    a.insert("Mio", {6, 5});
    a.insert("Azuza", {6, 7});
    b.insert("Eva", {3, 2});
    b.insert("Asuka", {4, 9});
    b.insert("Rei", {6, 7});
    a = b;
    a.erase("Asuka");
    ASSERT_TRUE(a != b);
}

TEST_F(F_test, Equality) {
    a.insert("K-ON!", {3, 2});
    a.insert("Mugi", {4, 5});
    a.insert("Ton", {6, 7});
    b = a;
    ASSERT_TRUE(a == b);
    b.erase("K-ON!");
    ASSERT_FALSE(a == b);
    ASSERT_TRUE(a != b);
    b.insert("K-ON!", {2, 1});
    ASSERT_FALSE(a == b);
}

TEST_F(F_test, Swap) {
    a.insert("K-ON!", {3, 0});
    a.insert("Yui", {6, 899});
    a.insert("Ritsu", {9, 7});
    b.insert("Eva", {56, 2});
    b.insert("Asuka", {4, 9});
    b.insert("Rei", {6, 6});
    a.swap(b);
    ASSERT_FALSE(a == b);
    b.swap(a);
    ASSERT_TRUE(a.contains("K-ON!"));
}

TEST_F(F_test, Clear) {
    a.insert("Guts", {3, 0});
    a.insert("Griffith", {6, 899});
    a.insert("Casca", {9, 7});
    ASSERT_EQ(3, a.get_size());
    a.clear();
    ASSERT_EQ(0, a.get_size());
    EXPECT_ANY_THROW(a.at("Guts"));
    EXPECT_ANY_THROW(a.at("Griffith"));
    EXPECT_ANY_THROW(a.at("Casca"));
}

TEST_F(F_test, Contains) {
    a.insert("MiA", {3, 0});
    a.insert("Riko", {6, 899});
    a.insert("Reg", {9, 7});
    FlatMap c(a);
    a.erase("MiA");
    a.erase("Riko");
    a.erase("Reg");
    ASSERT_EQ(false, a == c);
    EXPECT_ANY_THROW(a.at("MiA"));
    EXPECT_ANY_THROW(a.at("Riko"));
    EXPECT_ANY_THROW(a.at("Reg"));
}

TEST_F(F_test, Array_overload) {
    Value c = {0,0};
    EXPECT_TRUE(a["Key"].weight == c.weight && a["Key"].age == c.age);
    c = {1, 2};
    a["Key"] = c;
    EXPECT_TRUE(c.weight == a["Key"].weight && c.age == a["Key"].age);
}

TEST_F(F_test, key_search) {
    a.insert("K-ON!", {3, 0});
    a.insert("Yui", {6, 899});
    a.insert("Ui", {9, 7});
    b = a;
    ASSERT_TRUE(a == b);
    Value check = {3, 0};
    a.erase("K-ON!");
    ASSERT_TRUE(check.age == b.at("K-ON!").age);
    ASSERT_TRUE(check.weight == b.at("K-ON!").weight);
    EXPECT_ANY_THROW(a.at("K-ON!"));
}

TEST_F(F_test, key_search_const) {
    a.insert("K-ON!", {3, 0});
    a.insert("Yui", {6, 899});
    a.insert("Ui", {9, 7});
    const FlatMap c(a);
    const Value check = {3, 0};
    ASSERT_TRUE(a == c);
    a.erase("K-ON!");
    ASSERT_TRUE(c.at("K-ON!").age == check.age);
    ASSERT_TRUE(c.at("K-ON!").weight == check.weight);
    EXPECT_ANY_THROW(a.at("K-ON!"));
}

int main() {
    testing::InitGoogleTest();
    return RUN_ALL_TESTS();
}
