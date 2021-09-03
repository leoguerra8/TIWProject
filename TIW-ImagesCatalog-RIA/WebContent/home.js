(function() {
	let categoriesList, categoryForm, updateModal, pageOrchestrator = new PageOrchestrator(); // main controller
	let startElement, dest;
	let updateQueue = [];
	let allCategories = [];
	const MAX_CATEGORIES = 9;
	let closeEvent, cancelEvent, confirmEvent;

	window.addEventListener("load", () => {
		if (sessionStorage.getItem("username") == null) {
			window.location.href = "index.html";
		} else {
			pageOrchestrator.start(); // initialize the components
			pageOrchestrator.refresh(); // display initial content
		}
	}, false);

	function toText(category) {
		var spacing = ">".repeat(category.code.length - 1);
		return spacing + " " + category.code + " " + category.name;
	}

	function belongsToSubtree(catCode, fatCode) {
		return fatCode.substring(0, catCode.length) == catCode;
	}

	function dragStartHandler(event) {
		startElement = event.target.closest("tr");
	}

	function dragOverHandler(event) {
		event.preventDefault();
		var dest = event.target.closest("tr");
		dest.className = "selected";
	}

	function dragLeaveHandler(event) {
		var dest = event.target.closest("tr");
		dest.className = "not-selected";
	}

	function dropHandler(event) {
		dest = event.target.closest("tr");
		var catId = startElement.getAttribute("categoryId");
		var oldCatCode = startElement.getAttribute("categoryCode");
		var oldFatId = startElement.getAttribute("fatherId");
		var newFatId = dest.getAttribute("categoryId");
		var newFatCode = dest.getAttribute("categoryCode");
		
		var isAllowed = true;
		if (newFatId == oldFatId || belongsToSubtree(oldCatCode, newFatCode) || newFatId == catId) { isAllowed = false; }
		if (isAllowed) {
			updateModal.show();
		} else {
			dest.className = "not-selected";
		}
	}

	function moveAndUpdate(catId, oldCatCode, newFatCode, oldFatId, newFatId) {
		var lastChildNewFatherCode = Math.max.apply(Math, allCategories.filter(c => c.fatherId == newFatId).map(function(c) { return c.code; }));
		if(!isFinite(lastChildNewFatherCode)) {
			lastChildNewFatherCode = "-1";
		}
		var lastChildOldFatherCode = "-1";
		var newCatCode;
		if (oldFatId) {
			lastChildOldFatherCode = Math.max.apply(Math, allCategories.filter(c => c.fatherId == oldFatId).map(function(c) { return c.code; }));
		}
		lastChildNewFatherCode = lastChildNewFatherCode.toString();
		var lastDigit = parseInt(lastChildNewFatherCode.charAt(lastChildNewFatherCode.length-1));
		// calculate new category code
		if (lastDigit == MAX_CATEGORIES) {
			window.alert("The number of sub-categories cannot be more than 9");
			this.pageOrchestrator.refresh();
		} else {
			if (lastChildOldFatherCode == newFatCode) {
				if (lastChildNewFatherCode == -1) { newCatCode = oldCatCode + "1"; }
				else newCatCode = oldCatCode + (lastDigit+1).toString();
			}
			if (lastChildNewFatherCode == "-1") { newCatCode = newFatCode + "1"; }
			else newCatCode = newFatCode + (lastDigit+1).toString();
		}

		// update categories
		var i = allCategories.findIndex(c => c.id == catId);
		allCategories[i].code = newCatCode;
		allCategories[i].fatherId = newFatId;
		var catChildren = allCategories.filter(function(c) { return c.code.substring(0, oldCatCode.length) == oldCatCode});
		catChildren.forEach(function(child) {
			child.code = newCatCode + child.code.substring(oldCatCode.length);
		});
		var lastBroCode = Math.max.apply(Math, allCategories.filter(c => c.fatherId == oldFatId).map(function(c) { return c.code; }));
		if (isFinite(lastBroCode) && lastBroCode.toString().localeCompare(oldCatCode) == 1) {
			lastBroCode = lastBroCode.toString();
			var j = allCategories.findIndex(c => c.code == lastBroCode);
			allCategories[j].code = oldCatCode;
			broChildren = allCategories.filter(function(c) { return c.code.substring(0, lastBroCode.length) == lastBroCode });
			broChildren.forEach(function(broChild) {
				broChild.code = oldCatCode + broChild.code.substring(lastBroCode.length);
			})
		}
	}

	// Constructors of view components
	function PersonalMessage(_name, _surname, messagecontainer) {
		this.name = _name;
		this.surname = _surname;
		this.show = function() {
			messagecontainer.textContent = "Utente corrente: " + this.name + " " + this.surname;
		}
	}

	function UpdateModal(_modal, _cancel, _confirm) {
		this.modal = _modal;
		this.cancel = _cancel;
		this.confirm = _confirm; 

		this.modal.querySelector("span").addEventListener('click', () => { this.close(); } );
		this.cancel.addEventListener('click', () => { this.close(); });
		this.confirm.addEventListener('click', () => { this.confirm(); });

		this.show = function() {
			this.modal.style.display = "block"; 
			dest.className = "not-selected";
		}

		this.close = function() {
			this.modal.style.display = "none";
			dest.className = "not-selected";
		}

		this.confirm = function() {
			if (startElement && dest) {
				var catId = startElement.getAttribute("categoryId");
				var oldCatCode = startElement.getAttribute("categoryCode");
				var oldFatId = startElement.getAttribute("fatherId");
				var newFatId = dest.getAttribute("categoryId");
				var newFatCode = dest.getAttribute("categoryCode");
				var isAllowed = true;
				if (newFatId == oldFatId || belongsToSubtree(oldCatCode, newFatCode) || newFatId == catId) { isAllowed = false; }
				if (isAllowed) {
					updateQueue.push({
					categoryId: catId,
					oldCategoryCode: oldCatCode,
					oldFatherId: oldFatId,
					newFatherId: newFatId,
					newFatherCode: newFatCode
					});
					moveAndUpdate(parseInt(catId), oldCatCode, newFatCode, parseInt(oldFatId), parseInt(newFatId));
					categoriesList.update(allCategories.sort(function(c1, c2) { return (c1.code).localeCompare(c2.code) }));
				}
			} else {
				this.close();
			}
			this.close();
		}

		this.reset = function() {
			this.modal.style.display = "none";
		}
	}

	function CategoriesList(_alert, _listcontainer, _listcontainerbody, _savebtn) {
		this.alert = _alert;
		this.listcontainer = _listcontainer;
		this.listcontainerbody = _listcontainerbody;
		this.savebtn = _savebtn;

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
							allCategories = [...categories];
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

		this.registerEvents = function(orchestrator) {
			this.savebtn.addEventListener('click', (e) => {
				if (updateQueue.length != 0) {
					var self = this;
					makeCallJson("POST", 'UpdateCategories', updateQueue,
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

		this.update = function(arrayCategories) {
			var row, cell;
			this.listcontainerbody.innerHTML = ""; // empty the table body
			var self = this;
			arrayCategories.forEach(function(category) {
				row = document.createElement("tr");
				row.setAttribute("draggable", "true");
				row.addEventListener("dragstart", dragStartHandler);
                row.addEventListener("dragover", dragOverHandler);
                row.addEventListener("dragleave", dragLeaveHandler);
				row.addEventListener("drop", dropHandler);
                row.setAttribute("id", category.id);
                row.setAttribute("code", category.code);
                cell = document.createElement("td");
                anchor = document.createElement("a");
                cell.appendChild(anchor);
                cell.textContent = toText(category);
                row.setAttribute('categoryId', category.id);
                row.setAttribute('categoryCode', category.code);
				row.setAttribute('fatherId', category.fatherId);
                row.appendChild(cell);
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
			closeEvent = new Event ("close");
			cancelEvent = new Event ("cancel");
			confirmEvent = new Event ("confirm");

			personalMessage = new PersonalMessage(sessionStorage.getItem("name"), sessionStorage.getItem("surname"),
				document.getElementById("id_user"));
			personalMessage.show();

			categoriesList = new CategoriesList(
				alertContainer,
				document.getElementById("id_listcontainer"),
				document.getElementById("id_listcontainerbody"),
				document.getElementById("id_savebtn"));
			categoriesList.registerEvents(this);

			categoryForm = new CategoryForm(document.getElementById("id_form"),
			document.getElementById("id_father"), formAlertContainer);
			categoryForm.registerEvents(this);

			document.querySelector("a[href='Logout']").addEventListener('click', () => {
				window.sessionStorage.removeItem("username");
			});

			updateModal = new UpdateModal(document.getElementById("id_modal"),
			document.getElementById("id_cancelbtn"), document.getElementById("id_confirmbtn"));
		}
		
		this.refresh = function() {
			alertContainer.textContent = "";
			categoriesList.reset();
			categoryForm.reset();
			categoriesList.show();
			categoryForm.show();
			updateModal.reset();
			startElement = undefined;
			dest = undefined;
			updateQueue = [];
		}
	}
})();