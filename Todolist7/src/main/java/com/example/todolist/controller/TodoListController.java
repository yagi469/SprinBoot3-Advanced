package com.example.todolist.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.todolist.dao.TodoDaoImpl;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.service.TodoService;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TodoListController {
	private final TodoRepository todoRepository;
	private final TodoService todoService;
	private final HttpSession session;
	// Todolist5で追加
	@PersistenceContext
	private EntityManager entityManager;
	TodoDaoImpl todoDaoImpl;
	
	@PostConstruct
	public void init() {
		todoDaoImpl = new TodoDaoImpl(entityManager);
	}
	
	@GetMapping("/todo")
	public ModelAndView showTodoList(ModelAndView mv, 
			@PageableDefault(page=0, size=5, sort="id") Pageable pageable) {
		// sessionから前回の検索条件を取得
		TodoQuery todoQuery = (TodoQuery)session.getAttribute("todoQuery");
		if (todoQuery == null) {
			// なければ初期値を使う
			todoQuery = new TodoQuery();
			// セッションへ保存
			session.setAttribute("todoQuery", todoQuery);
		}
		
		// sessionから前回のpageableを取得
		Pageable prevPageable = (Pageable)session.getAttribute("prevPageable");
		if (prevPageable == null) {
			// なければ@PageableDefaultを使う
			prevPageable = pageable;
			// セッションへ保存
			session.setAttribute("prevPageable", prevPageable);
		}
		
		// 一覧を検索して表示する
		mv.setViewName("todoList");
		
		Page<Todo> todoPage = todoDaoImpl.findByCriteria(todoQuery, prevPageable);
		mv.addObject("todoQuery", todoQuery);
		mv.addObject("todoPage", todoPage);
		mv.addObject("todoList", todoPage.getContent());
		
		return mv;
	}
	
	@PostMapping("/todo/create/form")
	public ModelAndView createTodo(ModelAndView mv) {
		mv.setViewName("todoForm");
		mv.addObject("todoData", new TodoData());
		session.setAttribute("mode", "create");
		return mv;
	}
	
	//ToDo追加処理(TodoList2で追加したものをTodolist3で改善)
	@PostMapping("/todo/create/do")
	public String createTodo(@ModelAttribute @Validated TodoData todoData, 
			BindingResult result, ModelAndView mv) {
		// エラーチェック
		boolean isValid = todoService.isValid(todoData, result, true);
		if (!result.hasErrors() && isValid) {
			// エラーなし
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo";
		} else {
			// エラーあり
			// mv.setViewName("todoForm");
			// mv.addObject("todoData", todoData);
			return "todoForm";
		}
	}
	
	@PostMapping("/todo/cancel")
	public String cancel() {
		return "redirect:/todo";
	}
	
	@GetMapping("/todo/{id}")
	public ModelAndView todoById(@PathVariable(name="id") int id, ModelAndView mv) {
		mv.setViewName("todoForm");
		Todo todo = todoRepository.findById(id).get();
		mv.addObject("todoData", todo);
		session.setAttribute("mode", "update");
		return mv;
	}
	
	@PostMapping("/todo/update")
	public String updateTodo(@ModelAttribute @Validated TodoData todoData,
			BindingResult result, Model model) {
		// エラーチェック
		boolean isValid = todoService.isValid(todoData, result, false);
		if (!result.hasErrors() && isValid) {
			// エラーなし
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo";
		} else {
			// エラーあり
			// model.addAttribute("todoData", todoData);
			return "todoForm";
		}
	}
	
	@PostMapping("/todo/delete")
	public String deleteTodo(@ModelAttribute TodoData todoData) {
		todoRepository.deleteById(todoData.getId());
		return "redirect:/todo";
	}
	
	// フォームに入力された条件でToDoを検索(Todolist4で追加、Todolist5で変更)
	@PostMapping("/todo/query")
	public ModelAndView queryTodo(@ModelAttribute TodoQuery todoQuery,
			BindingResult result, 
			@PageableDefault(page = 0, size = 5) Pageable pageable,
			ModelAndView mv) {
		mv.setViewName("todoList");
		Page<Todo> todoPage = null;
		if (todoService.isValid(todoQuery, result)) {
			// エラーがなければ検索
			// todoList = todoService.doQuery(todoQuery);
			// ↓
			// JPQLによる検索
			todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable);
			
			// 入力された検索条件をsessionに保存
			session.setAttribute("todoQuery", todoQuery);
			mv.addObject("todoPage", todoPage);
			mv.addObject("todoList", todoPage.getContent());
		} else {
			// mv.addObject("todoQuery", todoQuery);
			// エラーがあった場合検索
			mv.addObject("todoPage", null);
			mv.addObject("todoList", null);
		}
		return mv;
	}
	
	// ページリンク押下時
	@GetMapping("/todo/query")
	public ModelAndView queryTodo(@PageableDefault(page = 0, size = 5) Pageable pageable,
			ModelAndView mv) {
		// 現在のページ位置を保存
		session.setAttribute("prevPageable", pageable);
		
		mv.setViewName("todoList");
		
		// sessionに保存されている条件で検索
		TodoQuery todoQuery = (TodoQuery)session.getAttribute("todoQuery");
		Page<Todo> todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable);
		
		mv.addObject("todoQuery", todoQuery); // 検索条件表示用
		mv.addObject("todoPage", todoPage); // page情報
		mv.addObject("todoList", todoPage.getContent()); // 検索結果
		
		return mv;
	}
}
