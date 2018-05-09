package com.example.heartx.assistant.event;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;

/**
 * 事件模拟器管理类
 * Created by HeartX on 2018/5/6.
 */

public class ToucherManager {

    public static final int SELECTOR_TOUCHER_ID = 0;

    public static final int STANDARD_TOUCHER_ID = 1;

    //当前模拟事件的模式
    private int currentToucher = STANDARD_TOUCHER_ID;

    private Toucher mToucher;

    private static ToucherManager instance;

    private Consumer<Integer> mConsumer;

    private Observable<Integer> mObservable = Observable.create(new ObservableOnSubscribe<Integer>() {
        @Override
        public void subscribe(ObservableEmitter<Integer> e) throws Exception {
            e.onNext(currentToucher);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    });

    public static ToucherManager getInstance() {

        if (instance == null) {

            instance = new ToucherManager();
        }

        return instance;
    }

    /**
     *
     * @param type 代表不同类型的模式
     * @return
     */
    public Toucher getToucher(int type) {

        switch (type) {
            case SELECTOR_TOUCHER_ID:
                if (!(mToucher instanceof SelectorToucher)) {
                    //mToucher = new SelectorToucher();
                }
                break;

            case STANDARD_TOUCHER_ID:
                if (!(mToucher instanceof KeyToucher)) {
                    //mToucher = new KeyToucher();
                }
                break;
        }

        return mToucher;
    }

    /**
     *
     * @param type 代表不同类型的模式
     * @param mouseSprite 用传感器控制的鼠标
     * @return
     */
    public Toucher getToucher(int type, MouseSprite mouseSprite) {

        switch (type) {
            case SELECTOR_TOUCHER_ID:
                if (!(mToucher instanceof SelectorToucher)) {
                    mToucher = new SelectorToucher(mouseSprite);
                }
                break;

            case STANDARD_TOUCHER_ID:
                if (!(mToucher instanceof KeyToucher)) {
                    mToucher = new KeyToucher(mouseSprite);
                }
                break;
        }

        return mToucher;
    }

    public void init(Consumer<Integer> consumer) {
        mConsumer = consumer;
        mObservable.subscribe(consumer);
    }

    public void changeMode(int type){
        currentToucher = type;
        if (mObservable != null && mConsumer != null) {
            mObservable.subscribe(mConsumer);
        }
    }
}
