package myinstance.main;

import com.oocourse.spec3.main.Group;
import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.Person;

public class MyMessage implements Message {
    private int id;
    private int socialValue;
    private int type;
    private Person person1;
    private Person person2;
    private Group group;

    public MyMessage(int messageId, int messageSocialValue, Person
            messagePerson1, Person messagePerson2) {
        type = 0;
        group = null;
        id = messageId;
        socialValue = messageSocialValue;
        person1 = messagePerson1;
        person2 = messagePerson2;
    }

    public MyMessage(int messageId, int messageSocialValue, Person
            messagePerson1, Group messageGroup) {
        type = 1;
        person2 = null;
        id = messageId;
        socialValue = messageSocialValue;
        person1 = messagePerson1;
        group = messageGroup;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public int getSocialValue() {
        return this.socialValue;
    }

    @Override
    public Person getPerson1() {
        return person1;
    }

    @Override
    public Person getPerson2() {
        return person2;
    }

    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Message)) {
            return false;
        }
        MyMessage myMessage = (MyMessage) o;
        return this.id == myMessage.getId();
    }
}
