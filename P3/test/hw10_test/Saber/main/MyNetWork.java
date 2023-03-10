package main;

import com.oocourse.spec2.exceptions.EqualGroupIdException;
import com.oocourse.spec2.exceptions.EqualMessageIdException;
import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.GroupIdNotFoundException;
import com.oocourse.spec2.exceptions.MessageIdNotFoundException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.main.Group;
import com.oocourse.spec2.main.Message;
import com.oocourse.spec2.main.Network;
import com.oocourse.spec2.main.Person;
import exc.MyEqualGroupIdException;
import exc.MyEqualMessageIdException;
import exc.MyEqualPersonIdException;
import exc.MyEqualRelationException;
import exc.MyGroupIdNotFoundException;
import exc.MyMessageIdNotFoundException;
import exc.MyPersonIdNotFoundException;
import exc.MyRelationNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MyNetWork implements Network {
    private final HashMap<Integer, Person> people = new HashMap<>();
    private final HashMap<Integer, Group> groups = new HashMap<>();
    private final HashMap<Integer, Message> messages = new HashMap<>();
    private final HashSet<Edge> edges = new HashSet<>();
    private final UnionFind<Person> ufp = new UnionFind<>();

    public MyNetWork() {
    }

    @Override
    public boolean contains(int id) {
        return people.containsKey(id);
    }

    @Override
    public Person getPerson(int id) {
        if (contains(id)) {
            return people.get(id);
        }
        return null;
    }

    @Override
    public void addPerson(Person person) throws EqualPersonIdException {
        if (people.containsValue(person)) {
            throw new MyEqualPersonIdException(person.getId());
        }
        people.put(person.getId(), person);
        ufp.addElement(person);
    }

    // people???????????????
    // ???id1???id2????????????????????????
    //// ???id1???id2??????
    //// ???id1???id2??????????????????value
    // ??????id1/id2 person???linkage???????????????????????????????????????
    @Override
    public void addRelation(int id1, int id2, int value)
            throws PersonIdNotFoundException, EqualRelationException {
        if (!contains(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!contains(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else if (getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyEqualRelationException(id1, id2);
        }
        ((MyPerson) getPerson(id1)).addLink(getPerson(id2), value);
        ((MyPerson) getPerson(id2)).addLink(getPerson(id1), value);
        ufp.union(getPerson(id1), getPerson(id2));
        for (Group x: groups.values()) {
            if (x.hasPerson(getPerson(id1)) && x.hasPerson(getPerson(id2))) {
                ((MyGroup) x).addValue(value);
            }
        }
        edges.add(new Edge(getPerson(id1), getPerson(id2), value));
    }

    @Override
    public int queryValue(int id1, int id2)
            throws PersonIdNotFoundException, RelationNotFoundException {
        if (!contains(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!contains(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyRelationNotFoundException(id1, id2);
        }
        return getPerson(id1).queryValue(getPerson(id2));
    }

    @Override
    public int queryPeopleSum() {
        return people.size();
    }

    // ??????id1???id2????????????
    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (!contains(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!contains(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        }
        return ufp.findRoot(getPerson(id1)).equals(ufp.findRoot(getPerson(id2)));    // O(log*n)
    }

    // ???????????????????????????
    // ???????????????????????????????????????????????????????????????????????????+1
    @Override
    public int queryBlockSum() {
        return ufp.cntBlock();
    }

    // subgroup:
    // ?????????person????????????????????????person???????????????person??????????????????????????????
    // people????????????id?????????person???????????????subgroup????????????????????????subgroup?????????????????????id????????????
    // ???subgroup?????????????????????????????????
    // ??????????????????
    @Override
    public int queryLeastConnection(int id) throws PersonIdNotFoundException {
        if (!contains(id)) {
            throw new MyPersonIdNotFoundException(id);
        }
        Person p = getPerson(id);
        Person pr = ufp.findRoot(p);
        ArrayList<Edge> blockEdges = new ArrayList<>();
        HashSet<Person> blockPeople = new HashSet<>();
        blockPeople.add(p);
        UnionFind<Person> ufe = new UnionFind<>();
        ufe.addElement(p);
        for (Person x: people.values()) {
            if (x.equals(p)) { continue; }
            if (ufp.findRoot(x).equals(pr)) {    // isCircle -- O(log*n) ??? O(1)
                blockPeople.add(x);
                ufe.addElement(x);
            }
        }
        for (Edge x: edges) {
            if (blockPeople.contains(x.getPerson1())
                    && blockPeople.contains(x.getPerson2())) {
                blockEdges.add(x);
            }
        }
        Collections.sort(blockEdges);
        int ans = 0;
        int cnt = 0;
        int peopleCnt = blockPeople.size();
        for (Edge x: blockEdges) {
            if (ufe.findRoot(x.getPerson1()).equals(
                    ufe.findRoot(x.getPerson2()))) { continue; }
            ufe.union(x.getPerson1(), x.getPerson2());
            ans += x.getValue();
            cnt++;
            if (cnt >= peopleCnt - 1) { break; }
        }
        return ans;
    }

    @Override
    public void addGroup(Group group) throws EqualGroupIdException {
        if (groups.containsKey(group.getId())) {
            throw new MyEqualGroupIdException(group.getId());
        }
        groups.put(group.getId(), group);
    }

    @Override
    public Group getGroup(int id) {
        if (groups.containsKey(id)) {
            return groups.get(id);
        }
        return null;
    }

    // id2???group???people????????????????????????
    //// group????????????1111????????????id1???person??????id2???group???people??????
    //// ?????????????????????
    @Override
    public void addToGroup(int id1, int id2)
            throws GroupIdNotFoundException, PersonIdNotFoundException, EqualPersonIdException {
        if (!groups.containsKey(id2)) {
            throw new MyGroupIdNotFoundException(id2);
        } else if (!people.containsKey(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (getGroup(id2).hasPerson(getPerson(id1))) {
            throw new MyEqualPersonIdException(id1);
        }

        if (getGroup(id2).getSize() < 1111) {
            getGroup(id2).addPerson(getPerson(id1));
        }
    }

    @Override
    public int queryGroupPeopleSum(int id) throws GroupIdNotFoundException {
        if (!groups.containsKey(id)) {
            throw new MyGroupIdNotFoundException(id);
        }
        return getGroup(id).getSize();
    }

    @Override
    public int queryGroupValueSum(int id) throws GroupIdNotFoundException {
        if (!groups.containsKey(id)) {
            throw new MyGroupIdNotFoundException(id);
        }
        return getGroup(id).getValueSum();
    }

    @Override
    public int queryGroupAgeVar(int id) throws GroupIdNotFoundException {
        if (!groups.containsKey(id)) {
            throw new MyGroupIdNotFoundException(id);
        }
        return getGroup(id).getAgeVar();
    }

    // id2???group???people????????????????????????
    //// ???id1???person???id2???group???people?????????
    @Override
    public void delFromGroup(int id1, int id2)
            throws GroupIdNotFoundException, PersonIdNotFoundException, EqualPersonIdException {
        if (!groups.containsKey(id2)) {
            throw new MyGroupIdNotFoundException(id2);
        } else if (!people.containsKey(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!getGroup(id2).hasPerson(getPerson(id1))) {
            throw new MyEqualPersonIdException(id1);
        }
        getGroup(id2).delPerson(getPerson(id1));
    }

    @Override
    public boolean containsMessage(int id) {
        return messages.containsKey(id);
    }

    @Override
    public void addMessage(Message message) throws EqualMessageIdException, EqualPersonIdException {
        if (containsMessage(message.getId())) {
            throw new MyEqualMessageIdException(message.getId());
        } else if (message.getType() == 0 &&
                message.getPerson1().equals(message.getPerson2())) {
            throw new MyEqualPersonIdException(message.getPerson1().getId());
        }
        messages.put(message.getId(), message);
    }

    @Override
    public Message getMessage(int id) {
        if (!containsMessage(id)) {
            return null;
        }
        return messages.get(id);
    }

    // type == 0:
    // ????????????person1??????????????????person2??????socialValue????????????message???socialValue
    // ???messages?????????id???message
    // ??????????????????person2??????messages?????????
    // type == 1:
    // ???????????????????????????message???socialValue
    // ???messages?????????id???message
    @Override
    public void sendMessage(int id) throws RelationNotFoundException,
            MessageIdNotFoundException, PersonIdNotFoundException {
        if (!containsMessage(id)) {
            throw new MyMessageIdNotFoundException(id);
        }
        Message msg = getMessage(id);
        if (msg.getType() == 0) {
            if (!msg.getPerson1().isLinked(msg.getPerson2())) {
                throw new MyRelationNotFoundException(
                        msg.getPerson1().getId(), msg.getPerson2().getId());
            }
            msg.getPerson1().addSocialValue(msg.getSocialValue());
            msg.getPerson2().addSocialValue(msg.getSocialValue());
            ((MyPerson) msg.getPerson2()).addMessageHead(msg);
        } else if (msg.getType() == 1) {
            if (!(msg.getGroup().hasPerson(msg.getPerson1()))) {
                throw new MyPersonIdNotFoundException(msg.getPerson1().getId());
            }
            for (Person x: ((MyGroup) msg.getGroup()).getPeople()) {
                x.addSocialValue(msg.getSocialValue());
            }
        }
        messages.remove(id);
    }

    @Override
    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (!contains(id)) {
            throw new MyPersonIdNotFoundException(id);
        }
        return getPerson(id).getSocialValue();
    }

    @Override
    public List<Message> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        if (!contains(id)) {
            throw new MyPersonIdNotFoundException(id);
        }
        return getPerson(id).getReceivedMessages();
    }
}
