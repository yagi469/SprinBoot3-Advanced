package com.example.todolist.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.todolist.common.Utils;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;

@Service
public class TodoService {
    // --------------------------------------------------------------------------------
    // Todo のチェック
    // --------------------------------------------------------------------------------
    public boolean isValid(TodoData todoData, BindingResult result, boolean isCreate) {
        boolean ans = true;

        // 件名が全角スペースだけで構成されていたらエラー
        if (!Utils.isBlank(todoData.getTitle())) {
            if (Utils.isAllDoubleSpace(todoData.getTitle())) {
                FieldError fieldError = new FieldError(
                    result.getObjectName(),
                    "title",
                    "件名が全角スペースです");
                result.addError(fieldError);
                ans = false;
            }
        }

        // 期限が""ならチェックしない
        String deadline = todoData.getDeadline();
        if (!deadline.equals("")) {
            // yyyy-mm-dd形式チェック
            if (!Utils.isValidDateFormat(deadline)) {
                FieldError fieldError = new FieldError(
                    result.getObjectName(),
                    "deadline",
                    "期限を設定するときはyyyy-mm-dd形式で入力してください");
                result.addError(fieldError);
                ans = false;

            } else {
                // 過去日付チェックは新規登録の場合のみ
                if (isCreate) {
                    // 過去日付ならエラー
                    if (!Utils.isTodayOrFurtureDate(deadline)) {
                        FieldError fieldError = new FieldError(
                            result.getObjectName(),
                            "deadline",
                            "期限を設定するときは今日以降にしてください");
                        result.addError(fieldError);
                        ans = false;
                    }
                }
            }
        }

        return ans;
    }

    // --------------------------------------------------------------------------------
    // 検索条件のチェック
    // --------------------------------------------------------------------------------
    public boolean isValid(TodoQuery todoQuery, BindingResult result) {
        boolean ans = true;

        // 期限:開始の形式をチェック
        String date = todoQuery.getDeadlineFrom();
        if (!date.equals("") && !Utils.isValidDateFormat(date)) {
            FieldError fieldError = new FieldError(
                result.getObjectName(),
                "deadlineFrom",
                "期限：開始を入力するときはyyyy-mm-dd形式で入力してください");
            result.addError(fieldError);
            ans = false;
        }

        // 期限:終了の形式をチェック
        date = todoQuery.getDeadlineTo();
        if (!date.equals("") && !Utils.isValidDateFormat(date)) {
            FieldError fieldError = new FieldError(
                result.getObjectName(),
                "deadlineTo",
                "期限：終了を入力するときはyyyy-mm-dd形式で入力してください");
            result.addError(fieldError);
            ans = false;
        }
        return ans;
    }
}