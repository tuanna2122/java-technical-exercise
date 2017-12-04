package com.treasuredata.exercise;

public class Member {

    private String memberId;
    private int age;

    public Member(String memberId, int age) {
        this.memberId = memberId;
        this.age = age;
    }

    public String getMemberId() {
        return memberId;
    }

    public int getAge() {
        return age;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((memberId == null) ? 0 : memberId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof Member)) {
            return false;
        }
        Member member = (Member) o;
        if (this.memberId == null) {
            if (member.getMemberId() != null) {
                return false;
            }
        } else if (!this.memberId.equals(member.getMemberId())) {
            return false;
        }
        return true;
    }
}