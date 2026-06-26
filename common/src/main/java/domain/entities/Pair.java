package domain.entities;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pair<L, R> {
    private L first;
    private R second;

    public Pair(L l, R r) {
        this.first = l;
        this.second = r;
    }
}

