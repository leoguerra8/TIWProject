(function() {
	let categoriesList, categoryForm, pageOrchestrator = new PageOrchestrator(); // main controller

	window.addEventListener("load", () => {
		if (sessionStorage.getItem("username") == null) {
			window.location.href = "index.html";
		} else {
			pageOrchestrator.start(); // initialize the components
			pageOrchestrator.refresh(); // display initial content
		}
	}, false);

	const toText = (category) => {
		const spacing = ">".repeat(category.code.length - 1);
		return spacing + " " + category.code + " " + category.name;
	}

	// Constructors of view components
	function PersonalMessage(_name, _surname, messagecontainer) {
		this.name = _name;
		this.surname = _surname;
		this.show = function() {
			messagecontainer.textContent = "Utente corrente: " + this.name + " " + this.surname;
		}
	}

	function CategoriesList(_alert, _listcontainer, _listcontainerbody) {
		this.alert = _alert;
		this.listcontainer = _listcontainer;
		this.listcontainerbody = _listcontainerbody;

		this.reset = function() {
			this.listcontainer.style.visibility = "hidden";
		}
		
		this.show = function() {
			var self = this;
			makeCall("GET", "GetCategories", null, 
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							var categories = JSON.parse(req.responseText);
							if (categories.length == 0) {
								self.alert.textContent = "No categories yet!";
								return;
							}
							self.update(categories);
						} else if (req.status == 403) {
							window.location.href = req.getResponseHeader("Location");
							window.sessionStorage.removeItem("username");
						} else {
							self.alert.textContent = message;
						}
					}
				});
		}

		this.update = function(arrayCategories) {
			var row, linkcell, linkText, anchor;
			this.listcontainerbody.innerHTML = ""; // empty the table body
			var self = this;
			arrayCategories.forEach(function(category) {
				row = document.createElement("tr");
				row.setAttribute("draggable", "true");
				row.setAttribute("ondragstart", "dragStartHandler(event)");
                row.setAttribute("ondrop", "dropOverHandler(event)");
                row.setAttribute("ondragover", "allowDrop(event)");
                row.setAttribute("ondragenter", "dragEnter(event)");
                row.setAttribute("ondragleave", "dragLeave(event)");
                row.setAttribute("class", "spaceUnder");
                row.setAttribute("id", category.id);
                row.setAttribute("code", category.code);
                linkcell = document.createElement("td");
                anchor = document.createElement("a");
                linkcell.appendChild(anchor);
                linkText = document.createTextNode(toText(category));
                anchor.appendChild(linkText);
                anchor.setAttribute('cateogoryid', category.id);
                anchor.href = "#";
                row.appendChild(linkcell);
                self.listcontainerbody.appendChild(row);
			});
			this.listcontainer.style.visibility = "visible";	
		}
	}

	function CategoryForm(formId, _selector, _alert) {
		this.form = formId;
		this.alert = _alert;
		this.selector = _selector;

		this.reset = function() {
			this.form.reset();
			this.selector.visibility = "hidden";
		}

		this.registerEvents = function(orchestrator) {
			this.form.querySelector("input[type='button'].submit").addEventListener('click', (e) => {
				if (this.form.checkValidity()) {
					var self = this;
					makeCall("POST", 'CreateCategory', e.target.closest("form"),
					function(req) {
						if (req.readyState == XMLHttpRequest.DONE) {
							var message = req.responseText;
							if (req.status == 200) {
								orchestrator.refresh();
							} else if (req.status == 403) {
								window.location.href = req.getResponseHeader("Location");
								window.sessionStorage.removeItem("username");
							} else {
								self.alert.textContent = message;
								self.reset();
							}
						}
					});
				}
			})
		}

		this.show = function() {
			var self = this;
			makeCall("GET", "GetCategories", null, 
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							var categories = JSON.parse(req.responseText);
							self.update(categories);
						} else if (req.status == 403) {
							window.location.href = req.getResponseHeader("Location");
							window.sessionStorage.removeItem("username");
						} else {
							self.alert.textContent = message;
						}
					}
				});
		}

		this.update = function(arrayCategories) {
			var option;
			this.selector.innerHTML = ""; // empty the selection list
			var self = this;
			option = document.createElement("option");
			option.text = "NONE (crea una nuova radice)";
			option.value = -1;
			self.selector.appendChild(option);
			arrayCategories.forEach(function(category) {
				option = document.createElement("option");
				option.text = category.code + " " + category.name;
				option.value = category.id;
				self.selector.appendChild(option);
			});
			this.selector.style.visibility = "visible";	
		}
	}

	function PageOrchestrator() {
		var alertContainer = document.getElementById("id_alert");
		var formAlertContainer = document.getElementById("id_formalert");

		this.start = function() {
			personalMessage = new PersonalMessage(sessionStorage.getItem("name"), sessionStorage.getItem("surname"),
				document.getElementById("id_user"));
			personalMessage.show();

			categoriesList = new CategoriesList(
				alertContainer,
				document.getElementById("id_listcontainer"),
				document.getElementById("id_listcontainerbody"));

			categoryForm = new CategoryForm(document.getElementById("id_form"),
			document.getElementById("id_father"), formAlertContainer);
			categoryForm.registerEvents(this);

			document.querySelector("a[href='Logout']").addEventListener('click', () => {
				window.sessionStorage.removeItem("username");
			});
		}
		
		this.refresh = function() {
			alertContainer.textContent = "";
			categoriesList.reset();
			categoryForm.reset();
			categoriesList.show();
			categoryForm.show();
		}
	}
})();