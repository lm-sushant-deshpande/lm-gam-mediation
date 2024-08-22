package lemma.lemmavideosdk.common;

public interface DisplayableAdI {
    public enum Type{
        Video,
        Html
    }
    public Type getType();
}
