function toggleSidebar(){
	
	if($(".sidebar").is(":visible")){
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","1%");
	}
	else{
		$(".sidebar").css("display","block");
		$(".content").css("margin-left","20%");
	}
};


function deleteContact(cId)
 {
  		swal({
  	  	  title: "Are you sure?",
  	  	  text: "Once deleted, you will not be able to recover this contact!",
  	  	  icon: "warning",
  	  	  buttons: true,
  	  	  dangerMode: true,
  	  	})
  	  	.then((willDelete) => {
  	  	  if (willDelete) {	
			window.location="/user/delete/"+cId;
			
  	  	  } else {
  	  	    swal("Your contact is safe!");
  	  	  }
  	  	});
 };

 const search=()=>{
	 
	let query=$("#search-input").val();
	if(query=='')
	{
		$(".search-result").hide();

	}else{
		//sending request to server
		let url=`http://localhost:8080/search/${query}`;

		fetch(url)
		.then((response) =>{
			return response.json();
		})
		.then((data) =>{
			console.log(data);

			let text=`<div class='list-group'>`;

			data.forEach((contact)=>{

				text+=`<a href='/user/contact/${contact.cId}' class='list-group-item list-group-item-action'> ${contact.name}  </a>`
			});

			text+=`</div>`;

			$(".search-result").html(text);
			$(".search-result").show();
		});

		
	}
 }





